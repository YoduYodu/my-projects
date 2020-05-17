from django.urls import path

from . import apps

urlpatterns = [
    path('', apps.users, name='users'),
]
