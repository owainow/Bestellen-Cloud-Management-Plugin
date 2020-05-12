# Bestellen Cloud Management Plugin
[![Build Status](https://ci.jenkins.io/job/Plugins/job/veracode-scan-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/veracode-scan-plugin/job/master/)
[![Jenkins Version](https://img.shields.io/badge/Jenkins-2.20-green.svg?label=min.%20Jenkins)](https://jenkins.io/download/)
![JDK8](https://img.shields.io/badge/jdk-8-yellow.svg?label=min.%20JDK)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Bestellen - The cloud management plugin for clean-up and maintenance of nodes with EC2 and REST API support.

This plugin was developed as a final year project at Bournemouth University for my Software Engineering degree.

My dissertation with Project background, research, objectives, implementation and evaluation can be found here: WAITING FOR RELEASE (June)

This project was my first open source project and first Jenkins plugin, feel free to use it! Let me know on LinkedIn if you do! https://www.linkedin.com/in/oow/

Contact me for any questions or queries:
- Owain.Osborne@tigerbay.uk.com
- https://www.linkedin.com/in/oow/

## How to Contribute
Bestellen is currently closed for contribution until June. My submission date is the 29th of May 2020. Once this has passed contributions are welcome through any contribution methods through the repository. 

### Important:
Pending import into main JenkinsCI repository. The current repository is: https://github.com/owainow/Bestellen_Cloud_Management_Jenkins_Plugin

The limit to this plugin is that machines are only deleted if the slave names in jenkins match the name in the cloud (Which is good practice anyway).

## Getting Started:
To build the plugin, please use Maven 3.3.9 or above, with JDK 8. 
The plugin can be built using:

```console
> mvn clean package
```
### Intial setup:
Very few parameters are required to run the plugin intially. 
![Basic Setup](https://i.ibb.co/Pxdssbh/Screenshot-2020-05-12-at-13-22-31.png)

Exclusion is important however should be used sparingly. It is advised excluded nodes are only machines such as web servers that are not dynamically spun up. 

All deletion options except "Offline nodes" require additional information which can be entered further down the form. 


### Report generation
If report generation has been set to yes it is advisable that you also archive the result using the archive artefact plugin. All reports are saved in the job workspace with the most recent run in the "latestReport" folder and previous runs in the "previousReports". Setup for archiving is below:

![Archive Setup](https://i.ibb.co/x7qJXWz/Screenshot-2020-05-12-at-13-39-52.png)



