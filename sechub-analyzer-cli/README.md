# SecHub Analyzer CLI

SecHub Analyzer CLI looks for markers in files and reports back the location of the markers in the file. The markers are:

- Start: `NOSECHUB`
- End: `END-NOSECHUB`

Usage:

~~~
$ java -jar analyzer-0.0.0.jar
usage: analyzer
 -d,--debug          Show additional debug messages.
 -h,--help           Display this help.
 -p,--pretty-print   Format output as pretty print.

~~~

Example:

~~~
$ java -jar analyzer-0.0.0.jar -p example
{
  "findings" : {
    "example/test.txt" : [ {
      "end" : {
        "column" : 3,
        "line" : 9,
        "type" : "END"
      },
      "start" : {
        "column" : 3,
        "line" : 3,
        "type" : "START"
      }
    } ],
    "example/test2.txt" : [ {
      "end" : {
        "column" : 4,
        "line" : 13,
        "type" : "END"
      },
      "start" : {
        "column" : 7,
        "line" : 9,
        "type" : "START"
      }
    }, {
      "end" : {
        "column" : 23,
        "line" : 21,
        "type" : "END"
      },
      "start" : {
        "column" : 23,
        "line" : 17,
        "type" : "START"
      }
    } ]
  }
}
~~~

