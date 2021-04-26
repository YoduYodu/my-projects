import pytest
import requests

from pytest_bdd import scenarios, when, then

API = 'https://rickandmortyapi.com/api/character/'

scenarios('./features/app.feature', example_converters=dict(number=int, result=str))


@pytest.fixture
@when('the Lucky Draw API is queried with <number>')
def rm_response(number):
    response = requests.get(API + str(id))
    return response


@then('the response status code is 200')
def rm_response_code(rm_response):
    assert rm_response.status_code == 200


@then('the response shows <name>')
def rm_response_name(rm_response, name):
    assert name == rm_response.json()['name']



