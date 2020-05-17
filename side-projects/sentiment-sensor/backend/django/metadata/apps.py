import json
from django.apps import AppConfig
from django.http import HttpRequest, HttpResponse
from django.views.decorators.csrf import csrf_exempt
from pymongo import MongoClient


class MetadataConfig(AppConfig):
    name = 'metadata'


password = 'rAL7TKvq6G6HD3kG'
client = MongoClient("mongodb+srv://wenti:{}@cluster0-rj4l5.gcp.mongodb.net/test?retryWrites=true&w=majority&ssl=true"
                     "&ssl_cert_reqs=CERT_NONE".format(password))
collection_metadata = client.test.metadata


@csrf_exempt
def metadata(req: HttpRequest):
    md = collection_metadata.find_one(
        {'id': 'metadata'}
    )
    del md['_id']
    res = HttpResponse(json.dumps(md), content_type='application/json')
    res["Access-Control-Allow-Origin"] = "*"
    res['Access-Control-Allow-Methods'] = "'POST', 'OPTIONS', 'GET']"
    return res


