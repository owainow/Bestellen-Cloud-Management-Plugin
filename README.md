
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

The limit to this plugin is that machines are only deleted if the slave names in Jenkins match the name in the cloud (Which is good practice anyway).

## Getting Started:
To build the plugin, please use Maven 3.3.9 or above, with JDK 8.
The plugin can be built using:

```console
> mvn clean package
```
### Initial setup:
Very few parameters are required to run the plugin initially. 
![Basic Setup](https://i.ibb.co/Pxdssbh/Screenshot-2020-05-12-at-13-22-31.png)

Exclusion is important however should be used sparingly. It is advised excluded nodes are only machines such as web servers that are not dynamically spun up. 

All deletion options except "Offline nodes" require additional information which can be entered further down the form. 

### Bestellen Efficiency Calculation
It is a longer-term goal to create a measure to show when a dynamic machine is inefficient (time to connect, cloud size compared to VM size etc) however this is a metric that I want to develop carefully. Below you can see the current equation that I am testing. As a result the plugin offers a bespoke deletion option to use when along side a call or check on your clouds capacity (if your chosen cloud supports it) that will clear X amount of machines (User specified in config) when ran. The order is as follows. Offline machines -> Idle machines (24 hours) -> Long Connect time Machines (15 minutes). If there is no specified amount the plugin will delete as many as it can find. 

```
Efficiency Rating = (Time online / times used) x (GB size / Cloud size (GB)) /100
```
The table below shows the range for results of the above calculation.

| Range.        | Efficiency Rating     |
| ------------- |:---------------------:|
| 0 - 2         |  Extremely Efficient  |
| 2 - 4         |  Efficient            |
| 4 - 6         |  In-Efficient         |
| 6 - 8         |  Very in-efficient    |
| 8 - 10+       | Extremely in-efficient|




### Amazon EC2
Amazon EC2 uses the return values from each to delete to evaluate a successful deletion. The EC2 delete uses the aws CLI and will install in on unix systems if not found and will configure with the parameters passed through. If aws CLI is run the plugin assumes it is configured already. 

Currently the code supports single instance creation but not multiple instances that are generated due to JSON parsing. This is noted for future work.

### Private Cloud Support / HTTP Requests
This plugin has been built to be as applicable to clouds that support HTTP requests as possible in order to get and delete machines.
Within the JSON config form if you wanted to get the machines at the following address: https://my-json-server.typicode.com/owainow/privateAPI/machines and delete using the key parameter the configuration would be as below:

![HTTPConfig](https://i.ibb.co/bgty0KR/Screenshot-2020-05-19-at-18-37-58.png)

It is advised although not essential that you set credentials binding up as shown below in order to hash your passwords and usernames as the password fields on protect input. The password fields are still essential to fill with the data you woud like to use.

![HTTPConfig](https://i.ibb.co/0hn0TjH/Screenshot-2020-05-19-at-18-48-57.png" alt="Screenshot-2020-05-19-at-18-48-57)

### Report generation
If report generation has been set to yes it is advisable that you also archive the result using the archive artefact plugin. All reports are saved in the job workspace with the most recent run in the "latestReport" folder and previous runs in the "previousReports". Setup for archiving is below:

![Archive Setup](https://i.ibb.co/x7qJXWz/Screenshot-2020-05-12-at-13-39-52.png)

Reports are supplemented with a deleiton graph which will be generated once over two pieces of data are stored. The graph shows up to the previous 10 runs.


