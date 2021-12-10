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
   
def deleteNode(def node, excludeArray,safeType){
    
          if (excludeArray.contains(node)){
                       println ("=====================================================")
        println("The machine "+node+" has been entered in the exclude list and will not be deleted.")
        delSuccess = false;
             }
             
          else if (json.find{it.Name.Value==[[node]]}){ //While looping json data if that value matches the node name passed through
              
                    println ("=====================================================")
                         String id = json.find{it.Name.Value==[[node]]}.Instance
                          id = id.replaceAll("\\[", "").replaceAll("\\]","");
                          deletionMap = deletionMap+[(node):(id)]
                        println ("Instance Name is: " + node + " Instance ID is: " + id)
                        
                         println('Removing node from Jenkins...');
            
                        println ("Now running /usr/local/bin/aws ec2 terminate-instances --instance-ids ${id} --output json")

                        if (safeType == "true"){
                           for (aNode in hudson.model.Hudson.instance.nodes) {
                                   
                                if(aNode.name.equals(node)){
                         aNode.getComputer().setTemporarilyOffline(true,null); //Set node as offline for saftey 
                         println("Setting ${node} to offline for saftey.")
                         aNode.getComputer().doDoDelete(); // Delete the node from Jenkins
                             println("Jenkins node ${node} deleted.")
                             }
                                }
                        procDelete = "/usr/local/bin/aws ec2 terminate-instances --instance-ids ${id} --output json".execute().text
                        Returnjson = new groovy.json.JsonSlurper().parseText(procDelete)
                    
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
                       else if (safeType == "false"){
                           for (aNode in hudson.model.Hudson.instance.nodes) {
                                   
                                if(aNode.name.equals(node)){
                                    println("Would be setting $node to offline for saftey.")
                                     println(" Next the Jenkins node $node would be deleted.")
                                    println("Fiinally executing /usr/local/bin/aws ec2 terminate-instances --instance-ids ${id} --output json")
                                    println("Here the return code of the call would be returned and checked to see whether the machine was actually terminated.")
                                    delSuccess = true;
                                 }
                                }
                       }
                       
                      }
                       
                       else {
                               println ("=====================================================")
                                                           
                            println("Node: " +node +" could not be matched in the cloud")
                            println("It does meet the deletion requirements however is unable to be deleted")
                            println("It is possible that this is an Orpaned VM and requires manual investigation.")
                           delSuccess = false;
                           
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
                  
            for (aNode in hudson.model.Hudson.instance.nodes) {
                if (aNode.getComputer().isOffline() == true){
                deleteNode(aNode.name,excludeArray,safeType);
           
                 }   
         }
     }
     
        
  else if (deleteType == "busy") {
      println('==================== Deletion Option of VMs Not Accepting Tasks Commencing ====================');
        for (aNode in hudson.model.Hudson.instance.nodes) {
                if (aNode.getComputer().isAcceptingTasks() == false){
                   deleteNode(aNode.name,excludeArray,safeType);
                 }   
         }
     }
     
        
        
     else if (deleteType == "efficient") {
         println('==================== Deletion Option for Machines set to Efficent ====================');
          println('==================== Warning this is only a BETA based on data available to Jenkins ====================');
              
                int newvmCount = vmCount as Integer
              
               println('================================================================= ')
               println("Starting to delete the nodes that are currently offline.")
               println('================================================================= ')
               
              for (aNode in hudson.model.Hudson.instance.nodes) {
                   if (i < newvmCount){
                if (aNode.getComputer().isOffline() == true && i < newvmCount ){
                 status = deleteNode(aNode.name,excludeArray,safeType);
                   if (status == true){
                          i ++
                          println("Machines deleted so far: " + i)
                         }
                         else {
                             println("Deletion of "+ aNode.name + " failed!")
                             println("Machines deleted so far: " + i)
                         }
            }
           }
          }
        
       
            if (i < newvmCount){
                   println('================================================================= ')
                 println("Continuing to now delete the nodes that have been idle for over 86400ms (24 hours).")
                 println('================================================================= ')
                for (aNode in hudson.model.Hudson.instance.nodes) {
                      
                    if (aNode.getComputer().getIdleStartMilliseconds() > 86400 && i < newvmCount) {
                    status =  deleteNode(aNode.name,excludeArray,safeType);
                        if (status == true){
                          i ++
                          println("Machines deleted so far: " + i)
                         }
                         else {
                             println("Deletion of "+ aNode.name + " failed!")
                             println("Machines deleted so far: " + i)
                         }
                   }
                }
          }

                      if (i < newvmCount){
                 println('================================================================= ')
                 println("Continuing to now delete the nodes with connection times over 900ms (15 minutes).")
                 println('================================================================= ')
                    for (aNode in hudson.model.Hudson.instance.nodes) {
                     
                    if (aNode.getComputer().getConnectTime() > 900 && i < newvmCount) {
                         status = deleteNode(aNode.name,excludeArray,safeType);
                           if (status == true){
                          i ++
                          println("Machines deleted so far: " + i)
                         }
                         else {
                             println("Deletion of "+ aNode.name + " failed!")
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
                  println('No node label has been entered please re-visit your job configuration');
             }
              
               for (aNode in hudson.model.Hudson.instance.nodes) {
                if (aNode.getLabelString().equals(deleteLabel)){
                    deleteNode(aNode.name,excludeArray,safeType);
            
                 }   
         }
        
            }
           println("=====================================================") 
            return deletionMap;
         }
     }
   

