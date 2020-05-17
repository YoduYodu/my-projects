## Sentiment Sensor

### Core features
* This project will provide an API, which will take review texts from users and predict whether it's a positive review or negative, and send the result back to users.
* The server will also provide another API which will return 5 most recent reviews and their corresponding results, and send the response back to users.
### Optional features
* Another two APIs will send responses with total number pieces of texts that users has sent, and correctness percentage of total predictions made by the project. 
* The users will be asked whether an output is correct, the feedback will be sent back to server, which will be another API handling user feedbacks.
* Analysis on all requests that users made, e.g. how accurate this sensor made according to users' feedbacks, how many reviews are there in total. These will be handle solely in frontend, meaning after getting information from APIs, the frontend will visualization the info by using D3.js

### Language and Tools
* For backend, I will use Django to handle API and database.
* Specifically, GraphQL will need to be used for API implementation, and PyMongo for MongoDB handling.
* For frontend, React.js is needed for sending requests to server and D3.js for creating views.
* Frontend will be deployed on GitHub Pages server, and backend server will be deployed on AWS.

### Timeline

* Week 5(Sep-22): Set up the environment for NLP modeling, including cloud GPU setup, Jupyter env setup, etc.
* Week 6(Sep-27): Data collection / Parallel downloading / Creating a validation set, and Data cleaning, using the model to help us find data problems.
* Week 7-8(Oct-11): Handle multi-label datasets by data blocks API
* Week 9(__Milestone #1__)  Train the NLP model using my own dataset and understanding of NLP.
* Week 10(Oct-25): To learn embedding layer, and try to optimize the model with it.
* Week 11-12(Nov-8): To learn Dropout, Data augmentation, Batch normalization and convolutions, as well as data ethics
* Week 13(Nov-15): Implementing production code for backend, and deploying.
* Week 14(Nov-22): Implementing frontend and deploying.
* Week 15(__Milestone #2__): Finish side project, cutting loose ends.

## Log
### CLI
```shell script
python3 manage.py runserver
```

#### OCT 6
* Initialize project

#### OCT 14, 15
* Grabs all the text file.
* Split the source dataset into train and test, 10% for validation
* Initialize the model, get the learning rate plot.
* Train the model, fine tune it, and save the fine-tuned encoder.
* Train another model classifier using the saved encoder and train the model 4 times.
* Save the model(model.pth) for future use.

#### OCT 27
* Implemented a predict API and its endpoint.

#### NOV 2
##### /predictions
* POST /predictions 
 
Request:

```json
{
  "user_id": "foo",
  "text": "foo"
}
```

Response:
```json
{
    "prediction_id": "foo",
    "user_id": "foo",
    "text": "foo",
    "is_positive": true,
    "time_date": "23:32:34PM",
    "object": "prediction"
}
```

##### /accuracies
* POST /accuracies  

Request:

```json
{
  "prediction_id": "foo",
  "is_accurate": "YES/NO"
}
```

Response:

```json
{
  "prediction_id": "foo",
  "is_accurate": "YES/NO",
  "object": "accuracy"
}
```

##### /users
* GET /users?user_id=foo

Response:
```json
{
  "user_id": "foo",
  "password": "pK3f(dj",
  "prediction_ids": [],
  "object": "user"
}
```

#### Nov 16
* Modify Prediction model
* Add Accuracy model

#### Nov 18
* Finish predictions api

#### Nov 21
* Connect to MongoDb

#### Nov 22
* Insert prediction in MongoDb

#### Nov 23
* Update metadata document

#### Next:
* Add metadata collection in MongoDB
* Change fastai models path