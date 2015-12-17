# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='RightsPage',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('file_name', models.FileField(upload_to=b'rights_page')),
                ('last_changed', models.DateTimeField(auto_now=True)),
            ],
            options={
                'ordering': ['file_name'],
            },
        ),
        migrations.CreateModel(
            name='TranslatePage',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('file_name', models.FileField(upload_to=b'translate_page')),
                ('last_changed', models.DateTimeField(auto_now=True)),
            ],
            options={
                'ordering': ['file_name'],
            },
        ),
    ]
