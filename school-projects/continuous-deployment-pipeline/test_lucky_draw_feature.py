import pytest
import requests

from pytest_bdd import scenarios, when, then

API = 'https://rickandmortyapi.com/api/character/'

scenarios('./features/app.feature', example_converters=dict(number=int, result=str))


@pytest.fixture
@when('the Lucky Draw API is queried with <number>')
def luck_draw_response(number):
    response = requests.get(API + str(id))
    return response


@then('the response status code is 200')
def luck_draw_response_code(luck_draw_response):
    assert luck_draw_response.status_code == 200


@then('the response shows <result>')
def luck_draw_response_result(luck_draw_response, result):
    assert result == luck_draw_response



