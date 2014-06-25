
// PREDEFINED PATTERN
final def generalXml = /<XML>[\d\D]*?<\/XML>/
final def incompleteXML = /<XML>[\d\D]*?<\/XML>/
final def code3InXml = /,3,\d+<\/HEADER>/
final def xmlTag = /(<\/?[\w]*?>)/
final def arrayOfDigits = /,([\d]+?)/
final def obrAndObx = /(OBR\||OBX\|).*/
final def obr = /OBR\|.*/
final def obx = /OBX\|.*/
final def obxVol = /MDC_VOL_FLUID_DELIV_TOTAL/
final def hl7Separator = /\|/
final def headerTag = /(<\/HEADER>)/

// REFINERS given a str, return a refined str
def removeIncompleteXml = { xml ->
  return xml.replaceAll(/<XML>[\d\D]*<XML>/, "<XML>")
}
def removeAllWrapperLogPrefix = { content ->
  return content.replaceAll(/INFO.*?\d \| /, "")
}
def replaceAllLineBreaks = { content ->
  return content.replaceAll(/(\r?\n|\r\n?)/, "\n")
}
def xmlTagSurroundWithComma = { xml ->
  return xml.replaceAll(xmlTag, ",\$1,")
}
def xmlAddACommaBeforeHeaderTag = { xml ->
  return xml.replaceAll(headerTag, ",\$1")
}
def surroundDigitsWithSingleQuote = { xml ->
  return xml.replaceAll(arrayOfDigits, ",'\$1")
}
def replaceLineBreakToString = { xml ->
  return xml.replaceAll(/\n/, "<br/>")
}
def replaceHL7SepToComma = { str ->
  return str.replaceAll(hl7Separator, ",")
}

// FILTERS given a str, return true or false
def code3Filter = { str ->
  return str =~ code3InXml
}
def obxVolFilter = { str ->
  return str =~ obxVol
}


// REAL CONFIG
[
  CONTENT: [
    removeAllWrapperLogPrefix,
    replaceAllLineBreaks
  ],
  TASKS: [
    [
      active: true,
      name: "extract xml from log",
      description: "extract xml from log",
      pattern: generalXml,
      refiners: [
        removeIncompleteXml
      ],
      targets: [
        [
          active: false,
          name: "extract allXML", 
          file: "allXML.log",
        ],
        [
          active: false,
          name: "extract all xml with code 3",
          file: "code3XML.log",
          filters: [
            code3Filter
          ]
        ],
        [
          active: true,
          name: "extract all xml With Code 3 to csv", 
          file: "code3XML.csv",
          filters: [
            code3Filter
          ],
          refiners: [
            xmlTagSurroundWithComma,
            xmlAddACommaBeforeHeaderTag,
            surroundDigitsWithSingleQuote,
            replaceLineBreakToString
          ],
          withTitle: true
        ],
      ]
    ],
    [
      active: true,
      name: "extract OBR and OBX",
      description: "extract OBR and OBX from log",
      pattern: obrAndObx,
      targets: [
        [
          active: false,
          name: "extract all OBR and OBX", 
          file: "allObrAndObx.log",
        ],
        [
          active: true,
          name: "extract all OBR and OBX to csv", 
          file: "allObrAndObx.csv",
          refiners: [
            replaceHL7SepToComma
          ],
          withTitle: true
        ],
        [
          active: false,
          name: "extract all OBX with total vol infused",
          file: "allObxWithVol.log",
          filters: [
            obxVolFilter
          ],
        ],
        [
          active: true,
          name: "extract all OBX with total vol infused to csv",
          file: "allObxWithVol.csv",
          filters: [
            obxVolFilter
          ],
          refiners: [
            replaceHL7SepToComma
          ],
          withTitle: true
        ],
      ]
    ],
  ],
]
