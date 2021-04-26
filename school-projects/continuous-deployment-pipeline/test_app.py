from app import lucky_draw


def test_lucky_draw():
    assert 'Oh you are not so lucky' == lucky_draw(1)
    assert 'Geez you are extremely lucky' == lucky_draw(2)
    assert 'Please try one more time' == lucky_draw(3)
    assert 'Please try one more time' == lucky_draw(6)
