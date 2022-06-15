# Invoices - REST API

### Reference Documentation

### Technologies

* Java 17.0.3 (Amazon Corretto)
* Gradle 7.2
* Spring Boot 2.6.7

### How To Run

* Test the build with passing unit tests with 'gradle build'
* Start the container with 'gradle bootRun' or './gradlew bootRun'
* You can test the 'invoices' endpoint with POSTMAN with the 'Invoices-POSTMAN.json' template that's in the root project folder (import it as collection)
* There's also a screenshot of POSTMAN's POST request for a reference in case you're using some other method

### IntelliJ IDEA

* Set JDK from Project Structure to the according Java distribution (+ source compatability)
* Set Gradle -> Gradle JVM to the according Java version
* Set Compiler -> Annotation Processor -> Enable (Lombok support)

### TODO

_because out of time_
* Unit tests for InvoiceBucketRepository
* More test cases for InvoiceControllerTest