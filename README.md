# Traceable API Security Testing Plugin

## Introduction
<p align="justify">
In the world full of microservices, there are cosmic number of APIs that a single organization exposes for internal and external use. But with the advantage of APIs making microservices architecture possible, there also comes the downside of data abuse, exposure and security. More the APIs exposed, an organization becomes more and more vulnerable to API attacks such as the <a href="https://owasp.org/www-project-top-ten/">OWASPs Top 10</a>. To solve this problem <a href="www.traceable.ai">TRACEABLE AI</a> helps you by continuously securing your APIs by bringing you deep visibility, real-time protection, and threat analytics. Traceable AI combines distributed tracing and advanced context-based behavioral analytics to deliver modern API security to your cloud-native and API-based applications.
</p>

### What is AST?
<p align="justify">
<b>API security testing</b> helps in finding vulnerabilities in very early stages, giving developers and Product security engineers more time and context to prioritize mitigation of vulnerabilities and build the resilient systems by scanning APIs for vulnerabilities by changing the data in the existing api specifications as required to introduce vulnerabilities.
</p>

### Features
<li>Extensive security testing coverage for microservices and APIs.</li>
<li>Generate tests from - Live traffic, OpenAPI Specs or even Recorded traces.</li> 
<li>Insertion into DevSecOps with Scan initiation and Vulnerability Management.</li>
<li>Risk based prioritization using asset inventory, threat intel, predictive modeling.</li>
<li>Allow for virtual patching for exploits to shield while Dev creates the long term fix.</li>



## 1. Installation
This Jenkins plugin for AST allows to run AST scan as a job on local jenkins instance.

<ol>
<li>Navigate to "Manage Jenkins > Manage Plugins > Available".</li>
<li>Search for "Traceable AST".</li>
<li>Install the plugin.</li>
</ol>

## 2. Get Scan Token

To get your scan token go to [app.traceable.ai](https://app.traceable.ai/) and login.
<ol>
<li>Go to the API testing tab.</li>
<li>Press the generate scan button, a dialog appears.</li>
<li>In the dialog "Generate new Token" and remember/note the scan token you generate.</li>
</ol>

<img src="src/main/webapp/img/Readme_get_token.png"/>

## 3. Add Build Step


<ol>
    <li>To add AST scan job, create a new item in jenkins as a Freestyle project.</li>
    <li>Add Traceable AST as the build step for the job.</li>
    <img src="src/main/webapp/img/Readme_add_build.png"/>
    <li>Fill the configuration fields for the job.</li>
    <li>Click on Advanced button to fill additional configuration fields.</li>
    <li>Apply and Save.</li>
    
</ol>
Client Token and Traffic environment are required fields and Client Token is the same as the scan token we generated.
<img src="src/main/webapp/img/Readme_add_configuration.png"/>

## 4. View Traceable AST Report

<ol>
<li>Build a job, which will run a scan according to the configurations.</li>
<li>After the completion of the job, go to the Job's page.</li>
<li>the report of the scan will be available as the Traceable AST report tab.</li>
</ol>
The scan report shows the number of vulnerabilities found for each type of plugin category.

<img src="src/main/webapp/img/Readme_report.png"/>









