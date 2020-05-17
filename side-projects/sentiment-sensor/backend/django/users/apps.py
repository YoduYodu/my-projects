import json
from django.apps import AppConfig
from django.http import HttpRequest, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from pymongo import MongoClient


class UsersConfig(AppConfig):
    name = 'users'


password = 'rAL7TKvq6G6HD3kG'
client = MongoClient("mongodb+srv://wenti:{}@cluster0-rj4l5.gcp.mongodb.net/test?retryWrites=true&w=majority&ssl=true"
                     "&ssl_cert_reqs=CERT_NONE".format(password))
db = client.test
collection_users = db.users


@csrf_exempt
def users(req: HttpRequest):
    if req.method == 'GET':
        if len(req.GET.keys()) == 1:
            return do_get_user_data(req)
        elif len(req.GET.keys()) == 2:
            return do_get_user_cookie(req)

    elif req.method == 'POST':
        return do_post_sign_up_user(req)


def do_get_user_data(req: HttpRequest):
    props = req.GET
    user = collection_users.find_one(
        {'user_id': props['user_id']}
    )

    if not user:
        return _prepare_res({})
    del user['_id']
    return _prepare_res(user)


def do_get_user_cookie(req: HttpRequest):
    props = req.GET
    user = collection_users.find_one({
        'user_id': props['user_id'],
        'password': props['password'],
    })
    if not user:
        return _prepare_res({'user_id': ''})
    del user['_id']
    return _prepare_res(user)


def do_post_sign_up_user(req):
    props = json.loads(req.body.decode('utf-8'))

    # Check if user existed
    checked_user = collection_users.find_one({'user_id': props.get('user_id')})
    if checked_user:
        return _prepare_res({'user_id': ''})

    new_user = {
            'user_id': props.get('user_id'),
            'password': props.get('password'),
            'submissions': [],
            'user_submission': 0,
            'user_positive': 0,
            'user_negative': 0,
            'user_correct': 0,
            'user_incorrect': 0
    }

    collection_users.insert_one(new_user)
    del new_user['_id']
    return _prepare_res(new_user)


def add_prediction_id_to_user(user_id: str, prediction_id: str):
    collection_users.insert_one(
        {'user_id': user_id},
        {'$push': {'prediction_ids': prediction_id}}
    )


def update_user_metadata(user_id: str, is_positive: bool):
    collection_users.update_one(
        {'user_id': user_id},
        {'$inc': {
            'user_submission': 1,
            'user_positive': 1 if is_positive else 0,
            'user_negative': 0 if is_positive else 1,
        }}
    )


def update_user_feedback(user_id: str, is_correct: bool):
    if collection_users.find_one({'user_id': user_id}):
        collection_users.update_one(
            {'user_id': user_id},
            {'$inc': {
                'user_correct': 1 if is_correct else 0,
                'user_incorrect': 0 if is_correct else 1,
            }}
        )


def _prepare_res(json_obj: dict):
    res = HttpResponse(json.dumps(json_obj), content_type='application/json')
    res["Access-Control-Allow-Origin"] = "*"
    res['Access-Control-Allow-Methods'] = "'POST', 'OPTIONS', 'GET']"
    return res
