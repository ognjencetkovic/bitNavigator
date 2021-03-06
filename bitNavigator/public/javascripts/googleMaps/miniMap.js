/**
 * Created by ognjen.cetkovic on 21/09/15.
 */

var marker;
function initMap() {
    if(lat == undefined || lng == undefined){
        lat = 43.850;
        lng = 18.390;
    }
    var map = new google.maps.Map(document.getElementById('map-add-place'), {
        zoom: 14,
        center: {lat: lat, lng: lng}
    });



    var geocoder = new google.maps.Geocoder;
    var infowindow = new google.maps.InfoWindow;

    var draggable = true;
    if(typeof markerDraggable != 'undefined') {
        draggable = false;
    }

    marker = new google.maps.Marker({
        position: {lat: lat, lng: lng},
        map: map,
        draggable: draggable
    });

    if(typeof markerDraggable != 'undefined') {
        return;
    }

    document.getElementById('btn-add').addEventListener('click', function() {
        geocodeLatLng(geocoder, map, infowindow, marker.getPosition().lat() + "," + marker.getPosition().lng());
    });
}
function geocodeLatLng(geocoder, map, infowindow, ll) {

    var input = ll;
    var latlngStr = input.split(',', 2);
    var latlng = {lat: parseFloat(latlngStr[0]), lng: parseFloat(latlngStr[1])};

    geocoder.geocode({'location': latlng}, function(results, status) {
        if (status === google.maps.GeocoderStatus.OK) {
            if (results[1]) {
                infowindow.setContent(results[0].formatted_address);
                infowindow.open(map, marker);
                document.getElementById("address").value = results[0].formatted_address;
                document.getElementById("latitude").value = latlng.lat;
                document.getElementById("longitude").value = latlng.lng;
            } else {
                window.alert('No results found');
            }
        } else {
            window.alert('Geocoder failed due to: ' + status);
        }
    });
}