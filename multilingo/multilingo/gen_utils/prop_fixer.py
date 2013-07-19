import os
import sys

try:
    import rosetta.polib as polib
except:
    par_p = os.path.split(os.path.normpath(os.path.dirname(__file__)))[0]
    sys.path.insert(0, par_p)
    import rosetta.polib as polib







class PoFixer(object):
    def __init__(self, new_tree, old_tree):
        self.new_tree = new_tree
        self.old_tree = old_tree

    def run(self):
        all_langs = self.find_languages()
        if os.stat(self.new_tree).st_mtime < os.stat(self.old_tree).st_mtime:
            print 'new tree os older than old tree, you propably mixed the params up :)'
            sys.exit(1)
        for lang in ('en',):#all_langs:
            print 'Processing', lang
            self.do_language(lang)
        print
        print 'Dont forget to do the following in apps/multi_lingo'
        print '\tpython ../../manage.py makemessages -a'
        print '\tpython ../../manage.py compilemessages'
            
    def find_languages(self):
        l = os.listdir(self.new_tree)
        try:
            l.remove('.svn')
        except:
            pass
        return l
        
    def do_language(self, lang):
        old_file = os.path.join(self.old_tree, lang, 'LC_MESSAGES/django.po')
        new_file = os.path.join(self.new_tree, lang, 'LC_MESSAGES/django.po')

        old_props = []
        po = polib.pofile(old_file)
        #
        # Find old props
        #
        for entry in po:
            # do something with entry...
            if self.entry_is_property(entry):
                old_props.append(entry)

        #
        # Add old props to new file
        #
        po = polib.pofile(new_file)
        for old_entry in old_props:
            if old_entry.obsolete:
                old_entry.obsolete = 0
            if 'fuzzy' in old_entry.flags:
                old_entry.flags.remove('fuzzy')
            entry = po.find(old_entry.msgid)
            if entry:
                po.remove(entry)
                po.append(old_entry)
            else:
                # only found in old file
                po.append(old_entry)
                
        #
        # Set defaults for empty props
        #
        for entry in po:
            if entry.msgid.find('embedCreate_t') > -1:
                a = 3
            if self.entry_is_property(entry):
                if entry.obsolete:
                    entry.obsolete = 0
                if 'fuzzy' in entry.flags:
                    entry.flags.remove('fuzzy')
                if not self.is_valid_content(entry):
                    entry.msgstr = entry.msgid.split('[')[1].split(']')[0]
                
        po.save()
        

    def entry_is_property(self, entry):
        if entry.msgid[0] =='#' and entry.msgid.find('_t') > -1:
            return True
        else:
            return False
        
    def is_valid_content(self, entry):
        if not entry.msgstr:
            return False
        if entry.msgstr.find(u'_t') > -1:
            return False
        return True
        
        
        
if __name__ == '__main__':
    try:
        new_path = sys.argv[1]
        old_path = sys.argv[2]
    except:
        print
        print 'params are new_path old_path of locale dirs'
        sys.exit(1)
        
    pf = PoFixer(new_path, old_path)
    pf.run()
    