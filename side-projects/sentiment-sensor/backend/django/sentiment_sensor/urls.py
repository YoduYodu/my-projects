
from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('predictions/', include('predictor.urls')),
    path('metadata/', include('metadata.urls')),
    path('users/', include('users.urls'))
]
