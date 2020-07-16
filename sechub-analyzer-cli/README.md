# SecHub Analyzer CLI

SecHub analyzer CLI is a command line tool which analyzes source files.

__At the moment it only scans for sechub markers.__

## SecHub markers

SecHub Analyzer CLI looks for markers in files and reports back the location of those markers. The markers are:

- Start: `NOSECHUB`
- End: `END-NOSECHUB`

The markers have to be added in a comment. The symbols used to indicate a comment can differ from programming language to programming language. In general, only single line comments are supported.

Comment examples:

C

~~~
#include <stdio.h>

int main() {
  // NOSECHUB
  printf("Hello World!\n");
  // END-NOSECHUB
  return 0;
}
~~~

Java

~~~
public class HelloWorld
{
    public static void main(String args[]) {
      // NOSECHUB
      System.out.println("Hello World!");
      // END-NOSECHUB
    }
}
~~~

Python

~~~
#!/usr/bin/env python

def hello():
  # NOSECHUB
  print("Hello World!")
  # END-NOSECHUB

hello()
~~~

Except, both the slash-star comment `/* */` and the arrow comment `<!-- -->` can be in more than one line, but they have to be at the beginning of the comment.

Will **not** be detected:

~~~
#include <stdio.h>

int main() {
  /*
   NOSECHUB
  */
  printf("Hello World!\n");
  /*
  	END-NOSECHUB
  */
  return 0;
}
~~~

Will be detected:

~~~
public class HelloWorld
{
    public static void main(String args[]) {
      /* 
       * NOSECHUB
       */
      System.out.println("Hello World!");
      /* 
       * END-NOSECHUB
       */
    }
}
~~~

Arrow comment in XML are detected:

~~~
<?xml version = "1.0" encoding = "UTF-8" ?>
<animals>
   <!-- NOSECHUB 
   -->
   <animal>
      <name>Leopard</name>
      <binomial>Panthera pardus</binomial>
   </animal>
   <!-- END-NOSECHUB 
   -->
   <animal>
      <name>Lion</name>
      <binomial>Panthera leo</binomial>
   </animal>
</animals>
~~~

## Usage

~~~
$ java -jar analyzer-0.0.0.jar
usage: analyzer [-d] [-h] [-p]

Find markers in files.

 -d,--debug          Show additional debug messages.
 -h,--help           Display this help.
 -p,--pretty-print   Format output as pretty print.

Please report issues at https://github.com/daimler/sechub
~~~

### Example

~~~
$ java -jar analyzer-0.0.0.jar -p example/
{
  "noSecHubMarkers" : {
    "../../src/test/resources/example/example.xml" : [ {
      "end" : {
        "column" : 8,
        "line" : 16,
        "type" : "END"
      },
      "start" : {
        "column" : 8,
        "line" : 11,
        "type" : "START"
      }
    } ],
    "../../src/test/resources/example/hello_world.c" : [ {
      "end" : {
        "column" : 5,
        "line" : 6,
        "type" : "END"
      },
      "start" : {
        "column" : 5,
        "line" : 4,
        "type" : "START"
      }
    } ],
    "../../src/test/resources/example/hello_world.py" : [ {
      "end" : {
        "column" : 4,
        "line" : 6,
        "type" : "END"
      },
      "start" : {
        "column" : 4,
        "line" : 4,
        "type" : "START"
      }
    } ],
    "../../src/test/resources/example/HelloWorld.java" : [ {
      "end" : {
        "column" : 11,
        "line" : 7,
        "type" : "END"
      },
      "start" : {
        "column" : 11,
        "line" : 5,
        "type" : "START"
      }
    } ]
  }
}
~~~


## Development

### Build

When you want to build the analyzer CLI, just do:

~~~
sechub$ ./gradlew buildAnalyzerCLI
~~~


