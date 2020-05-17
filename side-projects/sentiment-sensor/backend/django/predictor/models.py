from django.db import models
import uuid


class Prediction(models.Model):
    prediction_id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    user_id = models.UUIDField()
    text = models.CharField(max_length=500)
    is_positive = models.BooleanField()
    date_time = models.DateTimeField()
    object = models.CharField(max_length=100)
