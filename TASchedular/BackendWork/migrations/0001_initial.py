# Generated by Django 4.0.3 on 2022-04-25 03:02

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='ClassList',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=20)),
            ],
        ),
        migrations.CreateModel(
            name='MyUser',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('username', models.CharField(max_length=20)),
                ('password', models.CharField(max_length=20)),
                ('name', models.CharField(max_length=20)),
                ('email', models.CharField(max_length=20)),
                ('address', models.CharField(max_length=72)),
                ('phone', models.CharField(max_length=10)),
            ],
        ),
        migrations.CreateModel(
            name='Permission',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
            ],
        ),
        migrations.CreateModel(
            name='Schedule',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
            ],
        ),
        migrations.CreateModel(
            name='Section',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('Class', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='BackendWork.classlist')),
                ('TA', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='BackendWork.myuser')),
                ('schedule', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='BackendWork.schedule')),
            ],
        ),
        migrations.CreateModel(
            name='PermissionAssignment',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('permissionAPIName', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='BackendWork.permission')),
                ('userID', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='BackendWork.myuser')),
            ],
        ),
        migrations.AddField(
            model_name='classlist',
            name='owner',
            field=models.ForeignKey(null=True, on_delete=django.db.models.deletion.CASCADE, to='BackendWork.myuser'),
        ),
    ]
