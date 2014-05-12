<html>
<body>
<script src="https://www.java.com/js/deployJava.js"></script>
        <script>
            var attributes = {
                code :'be.fedict.eid.applet.Applet.class',
                archive :'eid-applet-package-1.2.0.Beta2.jar',
                width :1800,
                height :300
            };
            var parameters = {
                TargetPage :'stop',
                AppletService : 'applet-service-sign;jsessionid=<%=session.getId()%>',
                BackgroundColor : '#ffffff'
            };
            var version = '1.6';
            deployJava.runApplet(attributes, parameters, version);
</script>
</body>
</html>
