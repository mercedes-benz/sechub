# SecHub Analyzer CLI

SecHub Analyzer CLI looks for markers in files and reports back the location of the markers in the file. The markers are:

- Start: `NOSECHUB`
- End: `END-NOSECHUB`

The markers have to be added in a comment. The symbols used to indicate a comment can differ from language to language. Only single line comments are supported.

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

Except, for the star comment `/* */` and the arrow comment `<!-- -->` which can be in more than one line, but have to be at the beginning of the comment:

Star comment in Java

~~~
public class HelloWorld
{
    public static void main(String args[]) {
      /* NOSECHUB
       *
       */
      System.out.println("Hello World!");
      /* END-NOSECHUB
       *
       */
    }
}
~~~

Arrow comment in XML

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

##### Build

~~~
sechub$ ./gradlew buildAnalyzerCLI
~~~

##### Usage

~~~
$ java -jar analyzer-0.0.0.jar
usage: analyzer [-d] [-h] [-p]

Find markers in files.

 -d,--debug          Show additional debug messages.
 -h,--help           Display this help.
 -p,--pretty-print   Format output as pretty print.

Please report issues at https://github.com/daimler/sechub
~~~

##### Example

~~~
$ java -jar analyzer-0.0.0.jar -p example/
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

