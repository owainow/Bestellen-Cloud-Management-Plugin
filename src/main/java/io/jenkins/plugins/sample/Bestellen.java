package io.jenkins.plugins.sample;

import com.itextpdf.text.BadElementException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class Bestellen extends Builder implements SimpleBuildStep {

    private final String exclude;
    public String [] excludeArray;
    public String deleteType;
    public String acloudType;
    public String adeleteLabel;
    public String agenReport;
    public Object nodeinfo;
    public String test;
    public String excludeString;
    public String getNodeinfo;
    public String getReportResult;
    public String avmCount;
    private  List excludeList;
    public List<String> returnArray = new ArrayList<>();
    public String afetchAPI;
    public String aapiPassword;
    public String aapiUsername;
    public String ajsonName;
    public String ajsonDeleteParam;
    public String getTitle;
    public Object newReport;
    public String deleteVMString;
    public String awsID;
    public String awsKey;
    public String awsRegion;
    
    @DataBoundConstructor
    public Bestellen(String Exclude, String goalType, String cloudType,String reportType, String deleteLabel, String vmCount, String fetchAPI,String apiUsername, String apiPassword,String jsonName, String jsonDeleteParam,String awsID,String awsKey,String awsRegion) {
        this.exclude = Exclude;
        this.deleteType=goalType;
        this.acloudType=cloudType;
        this.adeleteLabel = deleteLabel;
        this.avmCount=vmCount;
        this.afetchAPI=fetchAPI;
        this.aapiPassword=apiPassword;
        this.aapiUsername=apiUsername;
        this.ajsonName=jsonName;
        this.ajsonDeleteParam=jsonDeleteParam;
        this.agenReport=reportType;
        this.awsID=awsID;
        this.awsKey=awsKey;
        this.awsRegion=awsRegion;

    }

    @SuppressWarnings("unused")
    public String[] getExclude() {
    excludeString = String.valueOf(exclude);
    excludeArray = exclude.split(",");
        return excludeArray;
    }
   
    
    
    public class Nodelist extends GetAgents {
        public Nodelist(String Exclude, String goalType, String acloudType, String adeleteLabel,String avmCount, String afetchAPI, String aapiUsername,String aapiPassword,String ajsonName, String ajsonDeleteParam,String awsID,String awsKey,String awsRegion) {
            super(Exclude, goalType, acloudType, adeleteLabel,avmCount,afetchAPI,aapiUsername,aapiPassword,ajsonName,ajsonDeleteParam,awsID,awsKey,awsRegion);
        }
       
    public List<String> getnodes(String args) throws Exception  {
    returnArray = new GetAgents(exclude,acloudType,deleteType,adeleteLabel,avmCount,afetchAPI,aapiUsername,aapiPassword,ajsonName,ajsonDeleteParam,awsID,awsKey,awsRegion).setnodelocation(result); 
    getNodeinfo = returnArray.get(0);
    deleteVMString = returnArray.get(1);
        return returnArray;
           }
    }
   
     public class Reports extends GenerateReport {
        public Reports(String Exclude, String goalType, String acloudType, String adeleteLabel,String avmCount, String afetchAPI,String ajsonName, String ajsonDeleteParam,String deleteVMString) {
            super(Exclude, goalType, acloudType, adeleteLabel,avmCount,afetchAPI,ajsonName,ajsonDeleteParam,deleteVMString);
        }
    public String generateaReport(String args) throws BadElementException, IOException{
        newReport= new GenerateReport(exclude,acloudType,deleteType,adeleteLabel,avmCount,afetchAPI,ajsonName,ajsonDeleteParam,deleteVMString).runReport(title);
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
     
        listener.getLogger().println("Cloud type has been set too: " + acloudType.toUpperCase() );

        listener.getLogger().println("Deletion Type for this run is set too: " + deleteType.toUpperCase() );
     if(acloudType.equals("ec2")){
        if(!awsID.isEmpty()){
          listener.getLogger().println("AWS ID: " + awsID);
        }
        if(!awsKey.isEmpty()){

            listener.getLogger().println("AWS Key has been set.");
        }
  
        if( !awsRegion.isEmpty()){
            listener.getLogger().println("AWS Region: " + awsRegion);
        }
     }
     if(acloudType.equals("custom")){
        if(!aapiUsername.isEmpty()){
          listener.getLogger().println("API Username: " + aapiUsername);
        }
        if( !aapiPassword.isEmpty()){
          
            listener.getLogger().println("API Password has been set.");
        }
     }
         if (deleteType.equals("efficient")){
          listener.getLogger().println("Deletion amount has been set to: " + avmCount );
      }
          if (deleteType .equals("label")){
          listener.getLogger().println("Delete label has been set too: " + adeleteLabel );
      }
        if (agenReport.equals("true")) {
            listener.getLogger().println("Generating report option has been checked so a report will be generated at the conclusion of this run.");
        } else {
            listener.getLogger().println("Report generating is set to OFF for this run. A report will not be generated although results can stilll be viewed in the output." );
        }

          
          new RegexExclude().regexExclude(excludeList);
          listener.getLogger().println("Lists of Machines Excluded from this Scan: " + exclude );
       
        try {
            new Nodelist(exclude,acloudType,deleteType,adeleteLabel,avmCount,afetchAPI,aapiUsername,aapiPassword,ajsonName,ajsonDeleteParam,awsID,awsKey,awsRegion).getnodes(getNodeinfo);
        } catch (Exception ex) {
            Logger.getLogger(Bestellen.class.getName()).log(Level.SEVERE, "Failed to generate nodelist. Groovy scripts have failed. Please check your configuration", ex);
        }
        
         listener.getLogger().println("Lists of nodes found: " + "\n" + getNodeinfo );
       
         if (agenReport.equals("true")) {
             try{
                 listener.getLogger().println("\nNow generating a PDF for this run.");
                 new Reports(exclude,acloudType,deleteType,adeleteLabel,avmCount,afetchAPI,ajsonName,ajsonDeleteParam,deleteVMString).generateaReport(getTitle);
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

        public FormValidation checkLabel (@QueryParameter String goalType, @QueryParameter String deleteLabel )
                throws IOException, ServletException {
            if (goalType.equals("label")){
            if (deleteLabel.length() == 0)
                return FormValidation.error(Messages.Bestellen_DescriptorImpl_errors_missing());
            if (deleteLabel.length() < 1)
                return FormValidation.warning(Messages.Bestellen_DescriptorImpl_warnings_tooShort());
            }
                return FormValidation.ok();
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


