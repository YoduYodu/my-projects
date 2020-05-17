import datetime
import json
import uuid

from django.apps import AppConfig
from django.http import HttpResponse, HttpResponseNotAllowed, HttpRequest
from django.views.decorators.csrf import csrf_exempt
from fastai.text import untar_data, load_data, URLs, AWD_LSTM, text_classifier_learner
from pymongo import MongoClient

from users.apps import add_prediction_id_to_user, update_user_metadata, update_user_feedback


class PredictorConfig(AppConfig):
    name = 'predictor'
    verbose_name = 'Sentiment predictor'


bs = 12
path = untar_data(URLs.IMDB)
data_clas = load_data(path, 'data_clas.pkl', bs=bs)
learn = text_classifier_learner(data_clas, AWD_LSTM, drop_mult=0.5)
learn.load_encoder('fine_tuned_enc')
learn.load('fourth')

password = 'rAL7TKvq6G6HD3kG'
client = MongoClient("mongodb+srv://wenti:{}@cluster0-rj4l5.gcp.mongodb.net/test?retryWrites=true&w=majority&ssl=true"
                     "&ssl_cert_reqs=CERT_NONE".format(password))
db = client.test
predictions, users, metadata = db.predictions, db.users, db.metadata


@csrf_exempt
def predict(req: HttpRequest):
    if req.method == 'OPTIONS':
        return do_options(req)

    elif req.method == 'POST':
        props = json.loads(req.body.decode('utf-8'))

        if 'feedback' in props:
            update_metadata_feedback(props['feedback'])
            update_user_feedback(props['user_id'], props['feedback'])
            return HttpResponse()
        else:
            user_id = props.get('user_id')
            text = props.get('text')

            prediction = {
                'prediction_id': str(uuid.uuid4()),
                'user_id': user_id,
                'text': text,
                'is_positive': True if str(learn.predict(text)[0]) == 'pos' else False,
                'time_date': str(datetime.datetime.now()),
                'object': 'prediction'
            }
            predictions.insert_one(prediction)
            del prediction['_id']
            update_metadata(prediction['is_positive'])

            if user_id:
                add_prediction_id_to_user(user_id, prediction['prediction_id'])
                update_user_metadata(user_id, prediction['is_positive'])

            res = HttpResponse(json.dumps(prediction), content_type='application/json')
            res["Access-Control-Allow-Origin"] = "*"
            res['Access-Control-Allow-Methods'] = "'POST', 'OPTIONS','GET']"
            return res

    else:
        return HttpResponseNotAllowed(['GET', 'POST', 'OPTIONS'])


def update_metadata(is_positive):
    metadata.update_one(
        {'id': 'metadata'},
        {'$inc': {
            'total_submission': 1,
            'total_positive': 1 if is_positive else 0,
            'total_negative': 0 if is_positive else 1,
        }}
    )


def update_metadata_feedback(is_correct: bool):
    metadata.update_one(
        {'id': 'metadata'},
        {'$inc': {
            'total_correct': 1 if is_correct else 0,
            'total_incorrect': 0 if is_correct else 1,
        }}
    )


def do_options(req: HttpRequest):
    res = HttpResponse()
    res['Allow'] = "['GET', 'POST', 'OPTIONS']"
    return res
