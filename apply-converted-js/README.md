# 1ST STEP: import .js files
```
 <script src="i18n.js" data-type="library"></script>
 <script src="locale/default.js" data-type="library"></script>
 <script src="locale/ko.js" data-type="library"></script>
 ...
```

# 2ND STEP: set locale
```
 i18n._.setLocale('{your locale}');
```

# 3TH STEP: apply
```
 console.log(i18n._.text("{text key}", arg1, arg2...));
```
* Arguments are optional
* example: 
```
 // # locale/default.js
 i18n['default']['title']='TITLE';
 i18n['default']['message.blank']='{0} is null.';
```

```
 console.log(i18n._.text("title")); // print 'TITLE'
 console.log(i18n._.text("message.blank", "name")); // print 'name is null.'
```