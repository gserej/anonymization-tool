# Anonimization Tool

A Spring Boot based application created to help users to speed up the process of document redaction.

When the user upload either a PDF or an image file, content of the document is displayed on the main page and meanwhile
 the back-end application try to extract all the words from the document, categorize them based on the type 
 (dates, numbers, etc.) and send information about words locations to the front-end page. Next the user can select the
  type of information to redact and red boxes are displayed around these words. User then can click on boxes to turn them into 
 black ones to confirm selection. At the end when user clicks on the "Do anonymization" button, locations of marked boxes are sent
 back to the Spring application and it produces a link to the redacted PDF file.

Used technology stack:
- Java
- Spring Boot
- Spring MVC
- Maven
- Tess4J
- PDFBox
- JUnit 5
and more...

Running the project locally
====================
To run this application run the command: `mvn clean install spring-boot:run`

How To Use
====================
After running the application go to the http://localhost:8080/ page.


