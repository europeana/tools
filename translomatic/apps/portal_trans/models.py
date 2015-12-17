from django.db import models

from django.db.models.signals import pre_delete
from django.dispatch import receiver


# Create your models here.


def delete_file_if_exists(self):
    try:
        if self.id:
            #this = TranslatePage.objects.get(id=self.id)
            self.file_name.storage.delete(self.file_name.path)
    except:
        pass # when new file then we do nothing, normal case
    return


class TranslatePage(models.Model):
    """
    A page that should be translated
    """
    file_name = models.FileField(upload_to='translate_page')
    last_changed = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ['file_name']

    def __str__(self):
        return self.file_name.name

    def save(self, *args, **kwargs):
        # delete old file when replacing by updating the file
        delete_file_if_exists(self)
        super(TranslatePage, self).save(*args, **kwargs)


@receiver(pre_delete, sender=TranslatePage)
def TranslatePage_pre_delete_handler(sender, **kwargs):
    item = kwargs['instance']
    delete_file_if_exists(item)



class RightsPage(models.Model):
    file_name = models.FileField(upload_to='rights_page')
    last_changed = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ['file_name']

    def __str__(self):
        return self.file_name.name

    def save(self, *args, **kwargs):
        # delete old file when replacing by updating the file
        delete_file_if_exists(self)
        super(RightsPage, self).save(*args, **kwargs)


@receiver(pre_delete, sender=RightsPage)
def RightsPage_pre_delete_handler(sender, **kwargs):
    self = kwargs['instance']
    delete_file_if_exists(self)




"""
class MediaIMG(models.Model):
    file_name = models.FileField(upload_to='img')
    class Meta:
        ordering = ['file_name']
        verbose_name_plural = 'media img'

    def __unicode__(self):
        return self.file_name.name


class MediaJS(models.Model):
    file_name = models.FileField(upload_to=os.path.join(settings.MEDIA_FILE_PATH, 'js'))
    class Meta:
        ordering = ['file_name']
        verbose_name_plural = 'media js'

    def __unicode__(self):
        return self.file_name.name


class MediaCSS(models.Model):
    file_name = models.FileField(upload_to=os.path.join(settings.MEDIA_FILE_PATH, 'css'))
    class Meta:
        ordering = ['file_name']
        verbose_name_plural = 'media css'

    def __unicode__(self):
        return self.file_name.name


    class Template(models.Model):
        file_name = models.FileField(upload_to='templates', storage=FileSystemStorage(location=THIS_DIR))

        def __unicode__(self):
            return os.path.split(self.file_name.name)[1]

"""