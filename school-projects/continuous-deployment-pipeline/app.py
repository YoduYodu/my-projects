from flask import Flask

app = Flask(__name__)


@app.route('/<int:number>')
def lucky_draw(number):
    if number == 1:
        return 'Oh you are not so lucky'
    elif number == 2:
        return 'Geez you are extremely lucky'
    else:
        return 'Please try one more time'


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
