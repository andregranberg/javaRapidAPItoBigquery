# javaRapidAPItoBigquery

### Preparations ###

Step 1 - Create a Cloud Project on GCP

Step 2 - Make sure that billing is enabled for your Cloud project

Step 3 - Enable the Cloud Functions and Cloud Build APIs

Step 4 - Install and initialize the Cloud SDK. 

Step 5 - Update and install gcloud components: (using command below in bash)
gcloud components update


### Create a project ###

Step 1 - Create a directory on your local system for the function code:
mkdir ~/helloworld
cd ~/helloworld

Step 2 - Create the project structure to contain the source directory and source file
mkdir -p src/main/java/functions
touch src/main/java/functions/HelloWorld.java

Step 3 - Write java code
See HellowWorld.java in this repo

Step 4 - Create pom.xml 
cd ~/helloworld
touch pom.xml

Step 5 - Write specifications to pom.xml
See pom.xml in this repo



### Deploy HelloWorld.java from local machine to GCP ### 

Step 1 - Compile (command below in bash)
mvn compile

Step 2 (alternative 1) - Run locally (command below in bash)
mvn function:run 

Step 2 (alternative 2) - Run locally with classpath to dependencies at runtime (command below in bash)
mvn function:run -Dexec.classpathScope="runtime"

Step 3 - Deploy cloud functions (with maven) (command below in bash)
gcloud functions deploy my-first-function --entry-point functions.HelloWorld --runtime java11 --trigger-http --memory 512MB --allow-unauthenticated
