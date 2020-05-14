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
def awsKeyMask;
def system = System.getProperty("os.name").toLowerCase(); //Used to approve aws install if needed
   
def deleteNode(def aSlave, excludeArray){
    
          if (excludeArray.contains(aSlave)){
                       println ("=====================================================")
        println("The machine "+aSlave+" has been entered in the exclude list and will not be deleted.")
        delSuccess = false;
             }
          else if (json.find{it.Name.Value==[[aSlave]]}){ //While looping json data if that value matches the slave name passed through
              
                    println ("=====================================================")
                          id = json.find{it.Name.Value==[[aSlave]]}.Instance     
                          deletionMap = deletionMap+[(aSlave):(id)]
                        println ("Instance Name is: " + aSlave + " Instance ID is: " + id)
                        
                         println('Removing node from Jenkins...');
                       //   aSlave.getComputer().setTemporarilyOffline(true,null); //Set node as offline for saftey 
                       //   aSlave.getComputer().doDoDelete(); // Delet the node from Jenkins
                        println ("Now running /usr/local/bin/aws ec2 terminate-instances --instance-ids ${id} --output json")
                        
   
                       //def procDelete = "/usr/local/bin/aws ec2 terminate-instances --instance-ids ${id} --output json".execute().text
                       delSuccess = true;
                       }
                       
                       else {
                               println ("=====================================================")
                           println("Slave: " +aSlave +" could not be matched in the cloud")
                                   println("It does meet the deletion requirements however is unable to be deleted")
                            println("It is possible that this is an Orpaned VM and requires manual investigation.")
                           delSuccess = false;
                       }
                       return delSuccess;
}

    	def isUnix() { //Used to validate whether to run install script.

		return (system.indexOf("nix") >= 0 || system.indexOf("nux") >= 0 || system.indexOf("aix") > 0 );

	}


def selection(exclude,cloudType,deleteType,deleteLabel,vmCount,awsID,awsKey,awsRegion){
  
       if (awsKey) {
       awsKeyMask = awsKey.replaceAll(".+","${awsKey.charAt(0)}${awsKey.charAt(1)}*********"); //Replace all characters apart from the first two with *'s for security
       }

    def isUnix = isUnix()
    excludeArray= exclude.split(',')
 
 println("If you would like the script to install AWS CLI itself please ensure that Jenkins has sufficent Sudo permissions to install Amazon CLI without a password. Please also insure your configuration variables have been set." + "\n");

        if (exists){
    println ("Bestellen has found an instance of Amazon CLI already installed on the server" + "\n")
        }

else if(isUnix){
println("Installing AWS CLI now with ID: ${awsID}. Key: ${awsKeyMask} Region: ${awsRegion}")
       def String[] installArguments= [awsID,awsKey,awsRegion];
      
    File sourceFile = new File("src/main/java/io/jenkins/plugins/sample/installAwsCli.groovy");
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
                deleteNode(aSlave.name,excludeArray);
            
                 }   
         }
     }
     
        
  else if (deleteType == "busy") {
      println('==================== Deletion Option of VMs Not Accepting Tasks Commencing ====================');
        for (aSlave in hudson.model.Hudson.instance.slaves) {
                if (aSlave.getComputer().isAcceptingTasks() == false){
                   deleteNode(aSlave.name,excludeArray);
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
                 status = deleteNode(aSlave.name,excludeArray);
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
                    status =  deleteNode(aSlave.name,excludeArray);
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
                         status = deleteNode(aSlave.name,excludeArray);
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
                    deleteNode(aSlave.name,excludeArray);
            
                 }   
         }
        
            }
           println("=====================================================") 
            return deletionMap;
         }
     }
   

