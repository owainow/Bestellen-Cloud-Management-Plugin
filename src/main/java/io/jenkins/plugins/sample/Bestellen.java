package io.jenkins.plugins.sample;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class Bestellen extends Builder implements SimpleBuildStep {

    private final String exclude;
    public  String [] excludeArray;
    public final String deleteType;
    public final String cloudType;
    public final String deleteLabel;
    public final String genReport;
    public Object nodeinfo;
    public String excludeString;
    public String getNodeinfo;
    public String getReportResult;
    public final String vmCount;
    private  List excludeList;
    public List<String> returnArray = new ArrayList<>();
    public final String fetchAPI;
    public String apiPassword;
    public final String apiUsername;
    public final String jsonName;
    public final String jsonDeleteParam;
    public String getTitle;
    public Object newReport;
    public String deleteVMString;
    public final String awsID;
    public String awsKey;
    public final String awsRegion;
    public final String safeType;
    
    
    @DataBoundConstructor
    public Bestellen(String Exclude, String goalType, String cloudType,String reportType, String deleteLabel, String vmCount, String fetchAPI,String apiUsername, String apiPassword,String jsonName, String jsonDeleteParam,String awsID,String awsKey,String awsRegion,String safeType) {
        this.exclude = Exclude;
        this.deleteType=goalType;
        this.cloudType=cloudType;
        this.deleteLabel = deleteLabel;
        this.vmCount=vmCount;
        this.fetchAPI=fetchAPI;
        this.apiPassword=apiPassword;
        this.apiUsername=apiUsername;
        this.jsonName=jsonName;
        this.jsonDeleteParam=jsonDeleteParam;
        this.genReport=reportType;
        this.awsID=awsID;
        this.awsKey=awsKey;
        this.awsRegion=awsRegion;
        this.safeType=safeType;
        

    }

    @SuppressWarnings("unused")
    public String[] getExclude() {
    excludeString = String.valueOf(exclude);
    excludeArray = exclude.split(",");
        return excludeArray;
    }
    //add workspace to file path for return node info
    //call groovy script to move files from old folder to new folder.
    
    public class Nodelist extends ReturnNodeInfo {
        public Nodelist(String Exclude, String goalType, String cloudType, String deleteLabel,String vmCount, String fetchAPI, String apiUsername,String apiPassword,String jsonName, String jsonDeleteParam,String awsID,String awsKey,String awsRegion,FilePath workspace,String safeType) {
            super(Exclude, goalType, cloudType, deleteLabel,vmCount,fetchAPI,apiUsername,apiPassword,jsonName,jsonDeleteParam,awsID,awsKey,awsRegion,workspace,safeType);
        }
       
    public List<String> getnodes(String args) throws Exception  {
    returnArray = new ReturnNodeInfo(exclude,cloudType,deleteType,deleteLabel,vmCount,fetchAPI,apiUsername,apiPassword,jsonName,jsonDeleteParam,awsID,awsKey,awsRegion,workspace,safeType).setnodelocation(result); 
    getNodeinfo = returnArray.get(0);
    deleteVMString = returnArray.get(1);
        return returnArray;
           }
    }
   
     public class Reports extends GenerateReport {
        public Reports(String Exclude, String goalType, String cloudType, String deleteLabel,String vmCount, String fetchAPI,String jsonName, String jsonDeleteParam,String deleteVMString,FilePath workspace) {
            super(Exclude, goalType, cloudType, deleteLabel,vmCount,fetchAPI,jsonName,jsonDeleteParam,deleteVMString,workspace);
        }
    public String generateaReport(String args) throws BadElementException, IOException, InstantiationException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DocumentException{
        newReport= new GenerateReport(exclude,cloudType,deleteType,deleteLabel,vmCount,fetchAPI,jsonName,jsonDeleteParam,deleteVMString,workspace).runReport(title);
        getTitle = newReport.toString();
        return getTitle;
    } 
     }
    
    public class RegexExclude {
            public List regexExclude (List args){
        excludeArray = exclude.split("\\s*,\\s*");
         excludeList = Arrays.asList(excludeArray);
         return excludeList;
            }
    }
  
 

    public void perform (Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
     
        listener.getLogger().println("Cloud type has been set too: " + cloudType.toUpperCase() );
     
        listener.getLogger().println("Delete nodes has been set too: " + safeType.toUpperCase() );

        listener.getLogger().println("Deletion Type for this run is set too: " + deleteType.toUpperCase() );
     if(cloudType.equals("ec2")){
        if(!awsID.isEmpty()){
          listener.getLogger().println("AWS ID: " + awsID);
        }
        if(!awsKey.isEmpty()){

            listener.getLogger().println("AWS Key has been set: " + awsKey);
        }
  
        if( !awsRegion.isEmpty()){
            listener.getLogger().println("AWS Region: " + awsRegion);
        }
     }
     if(cloudType.equals("custom")){
        if(!apiUsername.isEmpty()){
          listener.getLogger().println("API Username: " + apiUsername);
        }
        if( !apiPassword.isEmpty()){
          
            listener.getLogger().println("API Password has been set: "+apiPassword);
        }
     }
         if (deleteType.equals("efficient")){
          listener.getLogger().println("Deletion amount has been set to: " + vmCount );
      }
          if (deleteType .equals("label")){
          listener.getLogger().println("Delete label has been set too: " + deleteLabel );
      }
        if (genReport.equals("true")) {
            listener.getLogger().println("Generating report option has been checked so a report will be generated at the conclusion of this run.");
        } else {
            listener.getLogger().println("Report generating is set to OFF for this run. A report will not be generated although results can stilll be viewed in the output." );
        }

          
          new RegexExclude().regexExclude(excludeList);
          listener.getLogger().println("Lists of Machines Excluded from this Scan: " + exclude );
       
        try {
            new Nodelist(exclude,cloudType,deleteType,deleteLabel,vmCount,fetchAPI,apiUsername,apiPassword,jsonName,jsonDeleteParam,awsID,awsKey,awsRegion,workspace,safeType).getnodes(getNodeinfo);
        } catch (Exception ex) {
            Logger.getLogger(Bestellen.class.getName()).log(Level.SEVERE, "Failed to generate nodelist. Groovy scripts have failed. Please check your configuration", ex);
        }
        
         listener.getLogger().println("Lists of nodes found: " + "\n" + getNodeinfo );
       
         if (genReport.equals("true")) {
      
             try{
                 listener.getLogger().println("\nNow generating a PDF for this run.");
                 new Reports(exclude,cloudType,deleteType,deleteLabel,vmCount,fetchAPI,jsonName,jsonDeleteParam,deleteVMString,workspace).generateaReport(getTitle);
             }
             catch (Exception ex) {
            Logger.getLogger(Bestellen.class.getName()).log(Level.SEVERE, "Failed to generate report.", ex);
        }
             if(getTitle.isEmpty()){
                 listener.getLogger().println("\nThe PDF has failed to be created please investigate jenkins console log!");
             }
             else{
            listener.getLogger().println("The PDF title is: " +getTitle);
             }
          }
         else{
                    listener.getLogger().println("\nBestellen run has concluded.");
         }
    }
     


@Symbol("greet")
    @Extension
public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckExclude (@QueryParameter String value) throws IOException, ServletException {
    
            if(!value.isEmpty()&&value.length() < 2){
           
                return FormValidation.warning(Messages.Bestellen_DescriptorImpl_warnings_tooShortWarning());
                         }
            return FormValidation.ok();
        }
        
         public FormValidation doCheckFetchAPI (@QueryParameter String value) throws IOException, ServletException {
   
             if(!value.isEmpty() && value.length() < 5){
      
                return FormValidation.warning(Messages.Bestellen_DescriptorImpl_warnings_tooShortWarning());
              
          }
                
            return FormValidation.ok();
        }
          public FormValidation doCheckJsonName (@QueryParameter String value) throws IOException, ServletException {
         
             if( !value.isEmpty() && value.length() < 2){
           
                return FormValidation.warning(Messages.Bestellen_DescriptorImpl_warnings_tooShortWarning());
              
          }
            return FormValidation.ok();
        }
              public FormValidation doCheckJsonDeleteParam (@QueryParameter String value) throws IOException, ServletException {
         
             if( !value.isEmpty() && value.length() < 2){
           
                return FormValidation.warning(Messages.Bestellen_DescriptorImpl_warnings_tooShortWarning());
              
          }
            return FormValidation.ok();
        }
              
                public FormValidation doCheckDeleteLabel (@QueryParameter String value) throws IOException, ServletException {
         
             if(!value.isEmpty() && value.length() < 2){
           
                return FormValidation.warning(Messages.Bestellen_DescriptorImpl_warnings_tooShortWarning());
              
          }
            return FormValidation.ok();
        }
    
               public FormValidation doCheckVmCount (@QueryParameter String value) throws IOException, ServletException {
         try {
     Integer.parseInt(value);
            return FormValidation.ok();
          }
         catch (NumberFormatException e) {
    return FormValidation.error("Please enter a number in this field.");
  
  }
           
        }
               

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.Bestellen_DescriptorImpl_DisplayName();
        }
        
    }
   }


