from django.urls import path

from . import apps

urlpatterns = [
    path('', apps.predict, name='predictor'),
]
