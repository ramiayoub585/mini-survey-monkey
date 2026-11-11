![Build Status](https://github.com/ramiayoub585/mini-survey-monkey/actions/workflows/maven.yml/badge.svg)

# Mini-SurveyMonkey (SYSC4806 Milestone Project)
The Mini-SurveyMonkey is a small web app where a surveyor creates a survey with questions, users fill it out, and the surveyor closes it to see the compiled results.

## Azure Live App
[Mini-SurveyMonkey Link](https://mini-survey-monkey-sysc4806-cfdjhqdddzf2ckhu.westus-01.azurewebsites.net)

## Team Members (Group 25)
- Rami Ayoub
- Mohammad Ahmadi
- Naomi Amaya Lovett 
- Liam Bennet 
- Andrew Tawfik 

## How to Run
Requires Java 21 or later and Maven.
```
git clone https://github.com/ramiayoub585/mini-survey-monkey.git
cd mini-survey-monkey
mvn package
java -jar target/Mini-SurveyMonkey-1.0-SNAPSHOT.jar
```

## Milestone 1 (Current Phase)
An early prototype of the application. The application is not fully polished but the main functionalities are working and are the following: 

- **Survey Creation:** A surveyor is able to create a survey and manage a list of the created surveys. 
- **Questions:** A surveyor can add a question (3 types: Text, Number, Choice) for the respondents to answer. 
- **Filling Out Survey:** A respondent selects from the list of open surveys and fills out all questions for that survey. Once the survey is closed, they can no longer answer. 
- **Closing and Results:** The surveyor closes the survey which becomes unavailable for the respondents. A results page is shown to the surveyor with the complied answers to the survey questions. The text answers are listed and number and choice answers are counted.
- **Tests and Sample Data:** Startup tests run CI, integration tests are also added (see test folders). A sample survey can be loaded using the dev profile (DevSampleSurveys). 

## Next Sprint 
- Adding a method for surveyor authentication, ie. login credentials.
- Displaying the results of the survey through charts (example: histogram, pie charts, etc.).
- Giving confirmation to the user once they have successfully answered a survey. 
- Having the ability to edit/delete a question from a survey. 
- Styling the different pages making them look nicer and aligning them together. 
- Handle some bugs, ie. remove the ability to add a question when a survey is closed, displaying min/max values to respondent for number questions, etc.

## Database Schema 
///TO DO (Liam)///

## UML Diagram
///TO DO (Liam)///

## Project Status
[Mini-SurveyMonkey Board](https://github.com/users/ramiayoub585/projects/2)

| Column | Items                                                                                                                         |
|---|-------------------------------------------------------------------------------------------------------------------------------|
| Completed | Job 1 Setup and surveys, Job 2 Add questions to a survey, Job 3 Filling out, Job 4 Close and results and Job 5 Tests and demo |
| In progress | -                                                                                                                             |
| Backlog | -                                                                                                                             |

## Development Process
- Every feature is built on its own branch.
- Each change goes through a pull request reviewed by at least two other team members.
- CI (GitHub Actions) must pass before merging.
- Merged changes to `main` are deployed to Azure automatically.
- Weekly updates are posted in the "Weekly scrum" issues.




