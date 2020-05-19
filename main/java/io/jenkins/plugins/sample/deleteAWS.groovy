package io.jenkins.plugins.sample;

import groovy.json.*
/*
 * The MIT License
 *
 * Copyright 2020 tigerbaylimited.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 *
 * @author tigerbaylimited
 */

class deleteAWS {
    
def id; 
int i;
def filePath = "/usr/local/bin/aws/";
def file = new File(filePath)
def exists = file.exists();
def awsGet = "/usr/local/bin/aws ec2 describe-instances  --query Reservations[*].Instances[*].{Instance:InstanceId,Name:Tags} --output json".execute().text
def json = new groovy.json.JsonSlurper().parseText(awsGet)
def delSuccess;
def exclude;
def excludeArray;
def status;
def deletionMap = [:];
def awsRegion;
def awsID;
def awsKey;
def safeType;
def system = System.getProperty("os.name").toLowerCase(); //Used to approve aws install if needed
def key;
def procDelete;
def Returnjson;
def Firstkey;
def Secondkey;
def FirstReturnCode;
def SecondReturnCode;
def isUnix;
   
def deleteNode(def slave, excludeArray,safeType){
    
          if (excludeArray.contains(slave)){
                       println ("=====================================================")
        println("The machine "+slave+" has been entered in the exclude list and will not be deleted.")
        delSuccess = false;
             }
             
          else if (json.find{it.Name.Value==[[slave]]}){ //While looping json data if that value matches the slave name passed through
              
                    println ("=====================================================")
                         String id = json.find{it.Name.Value==[[slave]]}.Instance
                          id = id.replaceAll("\\[", "").replaceAll("\\]","");
                          deletionMap = deletionMap+[(slave):(id)]
                        println ("Instance Name is: " + slave + " Instance ID is: " + id)
                        
                         println('Removing node from Jenkins...');
            
                        println ("Now running /usr/local/bin/aws ec2 terminate-instances --instance-ids ${id} --output json")

                        if (safeType == "true"){
                           for (aSlave in hudson.model.Hudson.instance.slaves) {
                                   
                                if(aSlave.name.equals(slave)){
                         aSlave.getComputer().setTemporarilyOffline(true,null); //Set node as offline for saftey 
                         println("Setting {$slave} to offline for saftey.")
                         aSlave.getComputer().doDoDelete(); // Delete the node from Jenkins
                             println("Jenkins node {$slave} deleted.")
                             }
                                }
                       def procDelete = "/usr/local/bin/aws ec2 terminate-instances --instance-ids ${id} --output json".execute().text
                       def Returnjson = new groovy.json.JsonSlurper().parseText(procDelete)
                    
                      key = Returnjson.TerminatingInstances.CurrentState.Code
                     Firstkey = Returnjson.TerminatingInstances.CurrentState.Code
                     Secondkey = Returnjson.TerminatingInstances.PreviousState.Code
                     FirstReturnCode = Firstkey.findAll{it}
                     SecondReturnCode = Secondkey.findAll{it}
               
                   //This block checks the JSON returned by Amazon to see whether the machine was already in a terminated state as they can remain for up to 24 hours.
                   if (FirstReturnCode == [32] || [48] && SecondReturnCode != [48]){ //48 = Terminated and 32 = Shutting down
                               println("AWS reports the machine has been successfuly terminated.")
                                delSuccess = true;
                      }
                      else if (FirstReturnCode==[48] && SecondReturnCode == [48]){
                  
                          println("AWS reports Machine was already in a terminated state. This machine will not count towards the delete.")
                           delSuccess = false;
                       }
                      else{
                          println("No return code passed back delete failed")
                           delSuccess = false;
                      
                        }
                    
                      
                       }
                      }
                       
                       else {
                               println ("=====================================================")
                              
                              
                    
                           println("Slave: " +slave +" could not be matched in the cloud")
                                   println("It does meet the deletion requirements however is unable to be deleted")
                            println("It is possible that this is an Orpaned VM and requires manual investigation.")
                           delSuccess = false;
                           s
                       }
                       return delSuccess;

               }

    
 def isUnix() { //Used to validate whether to run install script as install script only supported on UNIX Machines (Jenkins pretty much always hosted on unix but needs to be checked).

		return (system.indexOf("nix") >= 0 || system.indexOf("nux") >= 0 || system.indexOf("aix") > 0 );

	}


def selection(exclude,cloudType,deleteType,deleteLabel,vmCount,awsID,awsKey,awsRegion,safeType){
    
    excludeArray= exclude.split(',')
       
  if (vmCount == null){
        vmCount = Integer.MAX_Value //If VM count has not been passed the script sets it delete as many as it finds.
  }
  //Below checks whether AWSCLI needs to be installed or not and if so calls install.
    isUnix = isUnix()
 
 println("If you would like the script to install AWS CLI itself please ensure that Jenkins has sufficent Sudo permissions to install Amazon CLI without a password. Please also insure your configuration variables have been set." + "\n");

        if (exists){
    println ("Bestellen has found an instance of Amazon CLI already installed on the server" + "\n")
        }

else if(isUnix){
println("Installing AWS CLI now with ID: ${awsID}. Key: ${awsKey} Region: ${awsRegion}")
       def String[] installArguments= [awsID,awsKey,awsRegion];
      
    File sourceFile = new File("src/main/java/io/jenkins/plugins/sample/installAWSCli.groovy");
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile);
    GroovyObject groovyObj = (GroovyObject) groovyClass.newInstance();
 
    groovyObj.invokeMethod("installCli", installArguments);
            
}
else{
    System.println("It would appear that you are running ${system}. Bestellen only supports automated install on Unix systems. Please install AWS manually.")

}
 
     
   if (deleteType == "offline" ) {
       println('==================== Deletion Option of Offline VMs Commencing ====================');
                  
            for (aSlave in hudson.model.Hudson.instance.slaves) {
                if (aSlave.getComputer().isOffline() == true){
                deleteNode(aSlave.name,excludeArray,safeType);
           
                 }   
         }
     }
     
        
  else if (deleteType == "busy") {
      println('==================== Deletion Option of VMs Not Accepting Tasks Commencing ====================');
        for (aSlave in hudson.model.Hudson.instance.slaves) {
                if (aSlave.getComputer().isAcceptingTasks() == false){
                   deleteNode(aSlave.name,excludeArray,safeType);
                 }   
         }
     }
     
        
        
     else if (deleteType == "efficient") {
         println('==================== Deletion Option for Machines set to Efficent ====================');
          println('==================== Warning this is only a BETA based on data available to Jenkins ====================');
              
                int newvmCount = vmCount as Integer
              
               println('================================================================= ')
               println("Starting to delete the slaves that are currently offline.")
               println('================================================================= ')
               
              for (aSlave in hudson.model.Hudson.instance.slaves) {
                   if (i < newvmCount){
                if (aSlave.getComputer().isOffline() == true && i < newvmCount ){
                 status = deleteNode(aSlave.name,excludeArray,safeType);
                   if (status == true){
                          i ++
                          println("Machines deleted so far: " + i)
                         }
                         else {
                             println("Deletion of "+ aSlave.name + " failed!")
                             println("Machines deleted so far: " + i)
                         }
            }
           }
          }
        
       
            if (i < newvmCount){
                   println('================================================================= ')
                 println("Continuing to now delete the slaves that have been idle for over 86400ms (24 hours).")
                 println('================================================================= ')
                for (aSlave in hudson.model.Hudson.instance.slaves) {
                      
                    if (aSlave.getComputer().getIdleStartMilliseconds() > 86400 && i < newvmCount) {
                    status =  deleteNode(aSlave.name,excludeArray,safeType);
                        if (status == true){
                          i ++
                          println("Machines deleted so far: " + i)
                         }
                         else {
                             println("Deletion of "+ aSlave.name + " failed!")
                             println("Machines deleted so far: " + i)
                         }
                   }
                }
          }

                      if (i < newvmCount){
                 println('================================================================= ')
                 println("Continuing to now delete the slaves that have been idle for over 900ms (15 minutes).")
                 println('================================================================= ')
                    for (aSlave in hudson.model.Hudson.instance.slaves) {
                     
                    if (aSlave.getComputer().getConnectTime() > 900 && i < newvmCount) {
                         status = deleteNode(aSlave.name,excludeArray,safeType);
                           if (status == true){
                          i ++
                          println("Machines deleted so far: " + i)
                         }
                         else {
                             println("Deletion of "+ aSlave.name + " failed!")
                             println("Machines deleted so far: " + i)
                         }
                   }
                }
                
     }
      if (i<newvmCount){
                    println('!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!')
                    println("COULD NOT FIND ENOUGH NON-EFFICENT VM'S TO DELETE ONLY: "+i+" machines deleted out of "+ newvmCount)
                    println('!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!')
                    }
                 
   
          }
                
               
           
      else if (deleteType == "label") {
          println('==================== Deletion Option of Label: '+ deleteLabel + ' Commencing ====================');
             if (deleteLabel == null){
                  println('No slave label has been entered please re-visit your job configuration');
             }
              
               for (aSlave in hudson.model.Hudson.instance.slaves) {
                if (aSlave.getLabelString().equals(deleteLabel)){
                    deleteNode(aSlave.name,excludeArray,safeType);
            
                 }   
         }
        
            }
           println("=====================================================") 
            return deletionMap;
         }
     }
   

