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
Requires Java 23 or later and Maven.
```
git clone https://github.com/ramiayoub585/mini-survey-monkey.git
cd mini-survey-monkey
mvn package
java -jar target/Mini-SurveyMonkey-1.0-SNAPSHOT.jar
```
To start with sample surveys (one open and one closed), run the app with the `dev` profile instead:
```
java -jar target/Mini-SurveyMonkey-1.0-SNAPSHOT.jar --spring.profiles.active=dev
```

## Milestone 2 (Current Phase)
The alpha release. Everything from Milestone 1 works, plus the features and fixes from the TA's feedback:

- **Surveys:** A surveyor creates surveys and manages them from one list, with each survey marked Open or Closed.
- **Questions:** A surveyor adds text, number (with a min and max) and multiple-choice questions, and can edit or delete them. Questions are locked once a survey is closed or has answers.
- **Filling Out a Survey:** A respondent picks an open survey, answers every question and sees a confirmation page. Answers are checked on the server, and number questions show their allowed range. Closed surveys can no longer be answered.
- **Closing and Results:** The surveyor closes a survey after confirming. The results page lists text answers, shows a histogram for number questions and a pie chart for multiple-choice questions, with the counts underneath.
- **Navigation and Styling:** Every page shares a header with links home, to Manage surveys and to Take a survey. All pages use one stylesheet, work at phone width, and show a friendly page for errors such as a missing or closed survey.
- **Database:** Data is stored in a persistent database on Azure, so surveys and responses survive a restart.
- **Tests:** Unit and integration tests are split into files by behaviour and run in CI on every pull request. Sample surveys load with the `dev` profile (`DevSampleSurveys`).

## Milestone 3 Plan
The final release, shown at the final demo:

- **Surveyor login:** Surveyors sign in, and each surveyor only sees and manages their own surveys. Respondents can still answer without an account.
- **Form accessibility:** Every question on the fill and add-question pages gets a proper label, so the forms work with screen readers and keyboards.
- **Demo feedback:** Fix the bugs and usability issues found at the Milestone 2 demo.
- **Tests:** Tests for each new feature, keeping CI green on every pull request.

## UML Diagram
![UML Class Diagram](docs/uml-class-diagram.png)

## Database Schema
Mini Survey Monkey uses Spring Data JPA with Hibernate ORM to manage database persistence. Mini Survey Monkey uses an H2 in-memory database where the database schema is automatically generated from the JPA entity classes when the application starts. 
The database is used to store surveys, questions, responses and answers.

### Main Entities
This table lists the entities and fields in the database.

| Entity | Fields | Description                                                                           |
|---|---|---------------------------------------------------------------------------------------|
| `Survey` | `id`, `title`, `active` | Stores a survey's identifier, title, and whether it is accepting responses.           |
| `Question` | `id`, `text`, `questionType`, `minValue`, `maxValue` | Stores questions belonging to surveys, including their type and number constraints.   |
| `SurveyResponse` | `id`, `survey` | Stores a completed survey submission referenced to the survey the submission answers. |
| `Answer` | `id`, `question`, `answer` | Stores an individual answer referenced to the question it answers.                    |

### Database Relationships
- **Survey → Question:** One-to-many. Each survey contains a list of questions. Questions are persisted using cascading operations.
- **Survey → SurveyResponse:** One-to-many. Each submitted response references a survey using a many-to-one relationship.
- **SurveyResponse → Answer:** One-to-many. Each response contains a list of answers, which are persisted using cascading operations.
- **Question → Answer:** One-to-many. Each answer references a question using a many-to-one relationship.

### Additional Database Tables
Hibernate also generates additional tables to persist collections and relationships:

- **Survey questions collection:** Maintains the association and ordering of questions within each survey.
- **Question options collection:** Stores the available options for multiple-choice questions and their order.
- **Survey response answers collection:** Maintains the association between survey responses and their answers.

### Question Types
Questions use the `QuestionType` enumeration, which has the following options:

- `TEXT`: Text responses.
- `NUMBER`: Numeric responses with optional min and max constraints.
- `CHOICE`: Multiple choice responses with a list of options.

### Database Configuration
Spring Boot configures the database connection, while Hibernate manages the database schema based on the JPA entity mappings.

Since the default database is in memory, its contents are not saved on application shut down.

## Project Status
[Mini-SurveyMonkey Board](https://github.com/users/ramiayoub585/projects/2)

| Column | Items |
|---|---|
| Completed | Job 1 Setup and surveys, Job 2 Add questions to a survey, Job 3 Filling out, Job 4 Close and results, Job 5 Tests and demo, Make repo public and protect main, Bundle 5 Results charts and multiple-choice fix |
| In progress | Bundle 1 Real database and schema, Bundle 2 Edit/delete questions, Bundle 3 Answer validation, confirmation and test cleanup, Bundle 4 Header, styling and README |
| Backlog | Milestone 3 plan above |

## Development Process
- Every feature is built on its own branch.
- Each change goes through a pull request reviewed by at least two other team members.
- CI (GitHub Actions) must pass before merging.
- Merged changes to `main` are deployed to Azure automatically.
- Weekly updates are posted in the "Weekly scrum" issues.




