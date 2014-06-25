/**
  *
  * @author DiHuang
  */
  
class ReadAndWriteFile {


  // Injected config object
  def config

  final String NEW_LINE = "\r\n"
  final String DOT = "."
  
  // files holder
  protected Map files = [:]
  
  // core function
  public boolean readAndWrite(File inputFile) {
    // retrieve content refiner
    def CONTENT_REFINER = config.get("CONTENT")
    
    // retrieve tasks
    def tasks = config.get("TASKS")
  
    // extract file name 
    String dirName = inputFile.name.replaceAll(/\./, "_")
    String parent = inputFile.parent
    
    // make dir
    File dir = makeDir(parent, dirName)
    
    
    boolean done = false
    
    try {
      def reader = inputFile
      def before = new Date()
      
      def content = reader.text
      
      def tb, ta, tgb, tga, ib, ia
      
      println "=>Content Refining \n"
      ib = new Date()
      CONTENT_REFINER.each { refiner ->
        content = refiner(content)
      }
      ia = new Date()
      println "<=Content Refining \n"
      
      tasks.each { task ->
        if(!task.active) return
          
        println "=>Task: ${task.name}\n"
        tb = new Date()
        def extracts = content.findAll( task.pattern )
        
        task.refiners.each { refiner ->
          extracts = extracts.collect( refiner )
          extracts = extracts.findAll()
        }
        
        task.targets.each { target ->
          if(!target.active) return
            
          def local = extracts.findAll()
        
          println "  ->Target: ${target.name}\n"
          tgb = new Date()
          def subFile = makeSubFile(dir, target.file, target.withTitle)
          
          println "     >>Filter\n"
          target.filters.each { filter ->
            local = local.findAll( filter )
          }
          println "     <<Filter\n"
          
          println "     >>Refiner\n"
          target.refiners.each { refiner ->
            local = local.collect( refiner )
            local = local.findAll()
          }
          println "     <<Refiner\n"
          
          println "     >>Write To ${subFile}\n"
          local.each { item ->
            writeToFile(subFile, item)
          }
          println "     <<Write To ${subFile}\n"
          
          tga = new Date()
          println "  <-End Target: ${formatTime(tgb, tga)}\n"
          
        }
        ta = new Date()
        println "<=Task: ${task.name}\n"  
      }
      
      def after = new Date()
      println "Total: cost ${formatTime(before, after)}"
      done = true
    } catch(Exception e) {
      e.printStackTrace()
      println("-----Something is wrong")
    }
    return done
  }
  
  // format time
  private String formatTime(Date before, Date after) {
    def len = after.getTime() - before.getTime()
    def min = len / 60000 as int
    def sec = (len % 60000) / 1000 as int
    def mils = ((len % 60000) % 1000) as int
    return "${min? min + ' min ': ''}${sec? sec + ' sec ': ''}${mils} mills"
  }
  
  // make dir
  private File makeDir(String parent, String dirName) {
    
    def dir
    if(parent == null)
      dir = new File(dirName)
    else
      dir = new File(parent, dirName)
      
    if(!dir.exists())
      dir.mkdir()
    return dir
  }
  // make sub file and init title
  private File makeSubFile(File dir, String fileName, Boolean withTitle) {
    File file = new File(dir, fileName)
    if(!file.exists())
      file.createNewFile()
    if(!withTitle)
      file.setText("")
    else {
      def title =""
      for(int i=1; i<=30; i++) 
        title += "C${i},"
      title += "\n"
      file.setText(title)
    }
    return file
  }  
  // simple write to file and write a new line following
  private writeToFile(File f, String s) {
    f.append(s)
    f.append(NEW_LINE)
  }
}

def operator = new ReadAndWriteFile()
def config = evaluate(new File("config.groovy"))
operator.config = config
// check parameter exists
if(! (args.size() > 0)) {
  println "Usage: no log file specified!"
  println "example: groovy ParseLog.groovy wrapper.log"
  System.exit(-1)
}
// check file exists
def file = new File(args[0])
if(!file.exists()) {
  println "Error: given file not exist"
  System.exit(-2)
}
// process
boolean done = false
println "\n>>>> START AT ${new Date()}\n"
done = operator.readAndWrite(file)
while(!done) {
  println("!!!!Sleep 5s and try again!")
  Thread.sleep(5000)
  done = operator.readAndWrite(file)
}
println "\n<<<< END AT ${new Date()}\n"
