# Generated by Django 4.0.3 on 2022-05-10 20:35

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('BackendWork', '0012_alter_section_endtime_alter_section_starttime'),
    ]

    operations = [
        migrations.AlterField(
            model_name='section',
            name='endTime',
            field=models.TimeField(blank=True, default=None, null=True),
        ),
        migrations.AlterField(
            model_name='section',
            name='startTime',
            field=models.TimeField(blank=True, default=None, null=True),
        ),
    ]