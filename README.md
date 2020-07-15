<<<<<<< HEAD
# Tracking-mongodb

Application de Tracking d'une activité sportife avec J2EE et MongoDB

## Login (LoginServlet):

![image](images/login.png "login")

- doGet :

In this method, we check if the session is already established, if so, we redirect the client to the activity page, and he does not need to identify himself. If the session is closed, we redirect it to the login page so that it can identify itself again.

````java
protected void doGet(HttpServletRequest request, HttpServletResponse response)
throws ServletException, IOException {

HttpSession session = request.getSession();

if (session.getAttribute("full_name") == null)
    this.getServletContext().getRequestDispatcher("/View/login.jsp").forward(request, response);
else
    this.getServletContext().getRequestDispatcher("/View/home.jsp").forward(request, response);
}
````

- doPost :

In this method, we establish a connection to our database, and we check whether the client exists or not. If the client exists, we create a session and redirect them to the activity page. If the customer does not exist, we display an error message on the web page.

````java
protected void doPost(HttpServletRequest request, HttpServletResponse response)
thows ServletException, IOException {

String email = request.getParameter("email");
String password = request.getParameter("pass");

// MongoDB URI
Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);
String uri = "mongodb://admin:walid@tracking-shard-00-00-mwydz.mongodb.net:27017,"
+ "tracking-shard-00-01-mwydz.mongodb.net:27017,"
+ "tracking-shard-00-02-mwydz.mongodb.net:27017/test?ssl=true&replicaSet=Trackingshard-0&authSource=admin&retryWrites=true&w=majority";

try {

// connection to database
MongoClient mongoClient = MongoClients.create(uri);

// get collection
MongoCollection<Document> database_collection = mongoClient.getDatabase("Tracking").getCollection("users");

// get email if exist
List<Document> userList = database_collection.find(and(eq("email", email), eq("pas
sword", password))).into(new ArrayList<Document>());

// verification
if (userList.isEmpty()) {
    request.setAttribute("loginError", "fail");
    this.getServletContext().getRequestDispatcher("/View/login.jsp").forward(request,response);
}
else {

    for (Document users : userList) {

        // create session
        HttpSession session = request.getSession();
        session.setAttribute("full_name", users.getString("full_name"));
        session.setAttribute("user_id", users.getString("user_id"));
        response.sendRedirect("/home");
    }
}

} catch (Exception e) {
// TODO: handle exception
}

}
````


## Sign Up (SignupServlet):

![image](images/signup.png "sign up")


- doPost :

In this method, we establish a connection to our database, and we first check whether the client exists or not, and we check afterwards if the password matches.

````java
protected void doPost(HttpServletRequest request, HttpServletResponse response)
throws ServletException, IOException {

// parameter POST request
String full_name = request.getParameter("full_name");
String email = request.getParameter("email");
String password = request.getParameter("pass");
String repeate_password = request.getParameter("rep_pass");

// MongoDB URI
Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);
String uri = "mongodb://admin:walid@tracking-shard-00-00-
mwydz.mongodb.net:27017,tracking-shard-00-01-mwydz.mongodb.net:27017,"
+ "tracking-shard-00-02-
mwydz.mongodb.net:27017/test?ssl=true&replicaSet=Tracking-shard-0"
+ "&authSource=admin&retryWrites=true&w=majority";

try {

// connection to database
MongoClient mongoClient = MongoClients.create(uri);

//get collection
MongoCollection<Document> database_collection = mongoClient.getDatabase("Tracking").getCollection("users");

// create Document
String uniqueID = UUID.randomUUID().toString();
Document user = new Document("_id", new ObjectId());
user.append("user_id", uniqueID)
    .append("full_name", full_name)
    .append("email", email)
    .append("password",password);

// get email if exist
List<Document> userList = database_collection.find(eq("email", email)).into(new ArrayList<Document>());

// verification
if (userList.isEmpty()) {

// verification of password
if (password.equals(repeate_password)) {

// insert data
database_collection.insertOne(user);

// create session and redirect to Home page
HttpSession session = request.getSession();
session.setAttribute("full_name", full_name);
session.setAttribute("user_id", uniqueID);
response.sendRedirect("/home");

} else {

// password not match
request.setAttribute("pass_no_match", "fail");
this.getServletContext().getRequestDispatcher("/View/signup.jsp").forward(request, response);

} 
}

else {

// email already exist
request.setAttribute("emailError", "fail");
this.getServletContext().getRequestDispatcher("/View/signup.jsp").forward(request, response);

}
}

catch (Exception e) {
// TODO: handle exception
e.getMessage();
System.out.println(e.getMessage());
} } }
````


## Activité

![image](images/activite.png "Activité")

This phase will allow the user to create his Activity, fill in the date and time of the start and end of his activity, as well as his route.

Note: for the route we used the following website:
https://www.mygpsfiles.com/app, which allows us to have the GPS points corresponding to the customer crossing points in an XML file(.tcx)

- getFilePath :

This method stores the file loaded by the client in the uploads folder.

````java
protected File getFilePath(HttpServletRequest request) throws Exception {

String file_path = null;
File uploadedFile = null;

// Retrieves <input type="file" name="file">
Part filePart = request.getPart("file"); 

// MSIE fix.
String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toS
tring(); 

InputStream fileContent = filePart.getInputStream();
byte[] buffer = new byte[fileContent.available()];
fileContent.read(buffer);
String root = getServletContext().getRealPath("/");
File path = new File(root + "/uploads");
if (!path.exists()) {
boolean status = path.mkdirs();
}
uploadedFile = new File(path + "/" + fileName);
OutputStream outStream = new FileOutputStream(uploadedFile);
outStream.write(buffer);
fileContent.close();
outStream.close();
return uploadedFile;
}
````

- getPosition :

This method converts the XML file loaded by the client into JSON by simply retrieving the necessary information (Latitude, Longitude).

````java
protected List<JSONObject> getPosition(String file) throws IOException {

List<Document> position = new ArrayList<>();
JSONObject pos = new JSONObject();File xmlfile = new File(file);
byte[] b = Files.readAllBytes(xmlfile.toPath());
String xml = new String(b);
JSONObject xmlJSONObj = XML.toJSONObject(xml);
String jsonPrettyPrintString = xmlJSONObj.toString();
System.out.println(jsonPrettyPrintString);

// browse the XML file
JSONArray obj = xmlJSONObj.getJSONObject("TrainingCenterDatabase").getJSONObject("Courses").getJSONObject("Course").getJSONObject("Track").getJSONArray("Trackpoint");

List<JSONObject> listdata = new ArrayList<JSONObject>();
for (int i = 0; i < obj.length(); i++) {
listdata.add(obj.getJSONObject(i).getJSONObject("Position"));
}
for (JSONObject string : listdata) {
position.add(new Document("Latitude", string.getDouble("LatitudeDegrees")).app
end("Longitude",string.getDouble("LongitudeDegrees")));
}
return listdata;
}
````

- Distance :

This method calculates the distance between the start point and the end point.

````java
private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

if ((lat1 == lat2) && (lon1 == lon2)) {
return 0;
}
else {
double theta = lon1 - lon2;
double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));dist = Math.acos(dist);

dist = Math.toDegrees(dist);
dist = dist * 60 * 1.1515;

if (unit.equals("K")) {
dist = dist * 1.609344;
} else if (unit.equals("N")) {
dist = dist * 0.8684;
}
return (dist);
} }
````

## MAP 

![image](images/map.png "Activité")

- map.jsp

In this part all the work is done in file map.jsp.
To use the JavaScript Maps API, you need an API key. To do this, you must create it on the site: https://cloud.google.com/console/google/maps-apis/overview

````javascript
<div id="map"></div>
<script>
var mapLatitude;
var mapLongitude;
var myLatlng;
function getLocation() {
if (navigator.geolocation) {
navigator.geolocation.getCurrentPosition(showPosition);
} else {
windows.alert("Geolocation is not supported by this browser.");
} }
function showPosition(position) {
console.log("showMapPosition");
mapLatitude = position.coords.latitude;
mapLongitude = position.coords.longitude;
myLatlng = new google.maps.LatLng(mapLatitude,mapLongitude);
initMap();}
function initMap() {
var mapOptions = {
zoom: 15,
center: new google.maps.LatLng(mapLatitude, mapLongitude)
};
map = new google.maps.Map(document.getElementById('map'),
mapOptions);
drowPath();
}
function drowPath(){
var arr = <% out.print(position); %>
//build the array
var triangleCoordsLS12 = []
for (var i=0; i<arr.length; i++) {
triangleCoordsLS12[i] = new google.maps.LatLng(arr[i]["LatitudeDegrees"], arr[i]["LongitudeDegrees"]);
}
//use the array as coordinates
bermudaTriangleLS12 = new google.maps.Polyline({
path: triangleCoordsLS12,
strokeColor: '#FF0000',
strokeOpacity: 3.5,
strokeWeight: 10,
geodesic: true
});
bermudaTriangleLS12.setMap(map);
}
</script>
<script src="https://maps.googleapis.com/maps/api/js?key=<KEYAPI>&callback=initMap" async defer></script>
````

## statistique 

![image](images/static.png "static")


In this phase we use the library FusionCharts to generate the statistics graph.

To use this library in J2EE download the java code (https://www.fusioncharts.com/download/fusioncharts-suite-xt?framework=java), then follow the steps below:

- Create a new package named as fusioncharts (Java resources → src → fusioncharts).

- Create a new java class named FusionCharts.

- Copy the file code fusioncharts-wrapper in the class create (fusioncharts-suite-xt → integrations → java → fusioncharts-wrapper).

````java
<div id = "chart" > </div>

<%
Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);
String uri = "mongodb://admin:walid@tracking-shard-00-00-
mwydz.mongodb.net:27017,"
+ "tracking-shard-00-01-mwydz.mongodb.net:27017,"
+ "tracking-shard-00-02-
mwydz.mongodb.net:27017/test?ssl=true&replicaSet=Tracking-shard-0"
+ "&authSource=admin&retryWrites=true&w=majority";

// connection to database
MongoClient mongoClient = MongoClients.create(uri);

// get collection
MongoCollection<Document> database_collection = mongoClient.getDatabase("Tracking").getCollection("Activity");

List<Document> userList = database_collection.find(Filters.eq("user_id", user_
id)).into(new ArrayList<Document>());

// store chart config name-config value pair
Map<String, String> chartConfig = new HashMap<String, String>();

//store label-value pair
Map<String, Integer> dataValuePair = new HashMap<String, Integer>();
chartConfig.put("caption", name+" Statistic");
chartConfig.put("subCaption", "All "+name+" Statistic for all Activity");
chartConfig.put("xAxisName", "Dates");
chartConfig.put("yAxisName", "Distance (Km)");
chartConfig.put("numberSuffix", "km");
chartConfig.put("theme", "fusion");
for (Document users : userList) {
dataValuePair.put(users.getString("date"), users.getInteger("distance"));
}

StringBuilder jsonData = new StringBuilder();
StringBuilder data = new StringBuilder();
// json data to use as chart data source
jsonData.append("{'chart':{");

for(Map.Entry conf:chartConfig.entrySet())
{
jsonData.append("'" + conf.getKey()+"':'"+conf.getValue() + "',");}
jsonData.replace(jsonData.length() - 1, jsonData.length() ,"},");
// build data object from label-value pair
data.append("'data':[");
for(Map.Entry pair:dataValuePair.entrySet())
{
data.append("{'label':'" + pair.getKey() + "','value':'" + pair.getValue() +"'},");
}

data.replace(data.length() - 1, data.length(),"]");
jsonData.append(data.toString());
jsonData.append("}");

// Create chart instance
// charttype, chartID, width, height,containerid, data format, data
FusionCharts firstChart = new FusionCharts(
"column2d",
"first_chart",
"700",
"400",
"chart",
"json",
jsonData.toString()
);
%>
<%= firstChart.render() %>
````


=======
# Tracking-mongodb
Application de Tracking d'une activité sportife avec J2EE et MongoDB
>>>>>>> d84523f13bf1b7f6d7319adf875b966b36ecfe62
