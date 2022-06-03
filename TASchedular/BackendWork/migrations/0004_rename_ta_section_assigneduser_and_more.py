# Generated by Django 4.0.3 on 2022-05-09 17:34

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('BackendWork', '0003_section_endtime_section_sectionnumber_and_more'),
    ]

    operations = [
        migrations.RenameField(
            model_name='section',
            old_name='TA',
            new_name='assignedUser',
        ),
        migrations.RemoveField(
            model_name='classlist',
            name='owner',
        ),
        migrations.AddField(
            model_name='classlist',
            name='term',
            field=models.CharField(choices=[('Summer', 'SUMMER'), ('Fall', 'FALL'), ('Winter', 'WINTER'), ('Spring', 'SPRING')], default='Fall', max_length=30),
        ),
        migrations.AddField(
            model_name='classlist',
            name='year',
            field=models.IntegerField(default=2022),
        ),
        migrations.AddField(
            model_name='myuser',
            name='assignedClass',
            field=models.ForeignKey(null=True, on_delete=django.db.models.deletion.SET_NULL, to='BackendWork.classlist'),
        ),
        migrations.AlterField(
            model_name='classlist',
            name='name',
            field=models.CharField(max_length=20, unique=True),
        ),
        migrations.AlterField(
            model_name='myuser',
            name='username',
            field=models.CharField(max_length=20, unique=True),
        ),
        migrations.AlterField(
            model_name='section',
            name='sectionType',
            field=models.CharField(choices=[('Discussion', 'DISCUSSION'), ('Lab', 'LAB'), ('Lecture', 'LECTURE')], default='Discussion', max_length=30),
        ),
    ]