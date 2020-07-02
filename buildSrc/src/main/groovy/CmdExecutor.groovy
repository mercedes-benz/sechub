// SPDX-License-Identifier: MIT
import org.gradle.api.*

class CmdExecutor{
    List<String> command = new ArrayList<String>();
    int timeOutInSeconds=-1;


    /**
    *  Executes given command list in given working directory. When started process
    *  does not return 0 as exit code a gradle exception is thrown which will break the build.
    *  The origin gradle exec process will always wait until no spawned processes are left.
    *  For e.g. the test integratino start this is an unwanted behaviour, because the process shall
    *  run and the next task (integration test execution) must proceed...
    */
    public void execute(File workingDir){
        /* why next lines so extreme ugly code (for next .. and get(x) )?
          becaus using just the list or converterting to array in standard
          java way  ala "cmdArray= list.toArray(new String[list.size])" does
          not work in groovy!!!! */
          String[] cmdarray = new String[command.size()];
          for (int i=0;i<cmdarray.length;i++) {
              cmdarray[i]=command.get(i);
          }
          println( ">> execute:" + command)
          /* create process */
          ProcessBuilder pb = new ProcessBuilder();
          pb.command(cmdarray);
          pb.directory(workingDir);
          pb.inheritIO();
          /* start */
          Process p = pb.start();
          if (timeOutInSeconds >-1){
              p.waitFor(timeOutInSeconds, java.util.concurrent.TimeUnit.SECONDS);
          }else{
              p.waitFor()
          }

          /* handle errors */
          int result = p.exitValue();
          if (result!=0) {
                println("Exit value of script was not 0. Output was:\n")
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                
                while ( (line = reader.readLine()) != null) {
                   println(line);
                }
                
                reader = new BufferedReader(new InputStreamReader(p.getErrgetErrorStream()));

                while ( (line = reader.readLine()) != null) {
                   println("ERROR:" + line);
                }
          
              throw new GradleException("Script returned exit code:$result");
          }
    }
}
