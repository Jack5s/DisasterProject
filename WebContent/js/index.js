var map;
$(document).ready(function() {
	mapInitialize();
	Initialize();
});

function mapInitialize() {
	map = new ol.Map({
		target : "map",
		layers : [ new ol.layer.Tile({
			source : new ol.source.OSM()
		}) ],
		view : new ol.View({
			center : ol.proj.fromLonLat([ 0, 0 ]),
			zoom : 4
		}),
	});

	var overlay = new ol.Overlay({
		element : document.getElementById('popupDiv'),
		autoPan : true,
		autoPanAnimation : {
			duration : 250
		}
	});
	map.addOverlay(overlay);

	var mousePositionControl = new ol.control.MousePosition({
		coordinateFormat : ol.coordinate.createStringXY(4),
		projection : 'EPSG:4326',
		target : document.getElementById('xydiv'),
		undefinedHTML : '&nbsp;'
	});
	map.addControl(mousePositionControl);

	// var scaleLineControl = new ol.control.ScaleLine({
	// target : document.getElementById('xydiv')
	// });
	// map.addControl(scaleLineControl);
	// map.addControl(mousePositionControl);

	map.on('pointermove', function(evt) {
		if (evt.dragging) {
			$(overlay).popover('hide');
			return;
		}
		var feature = map.forEachFeatureAtPixel(evt.pixel, function(feature) {
			return feature;
		});
		if (feature) {
			var coordinate = feature.getGeometry().getCoordinates();
			var textContent="Disaster Type: "+feature.get("disasterType");
			overlay.setPosition(coordinate);
			$('#popupDiv').attr('data-content', textContent);
			$('#popupDiv').popover('show');
		} else {
			$('#popupDiv').popover('hide');
		}
	});
	// return map;
}

function Initialize() {
	var pathName = window.document.location.pathname;
	var projectName = pathName
			.substring(1, pathName.substr(1).indexOf("/") + 1);
	var urlStr = "/" + projectName + "/index";

	$(".btn").click(function() {
		// alert( $(this).attr("id"));
		$.ajax({
			url : urlStr,
			data : {
				"web" : $(this).attr("id")
			},
			async : true,
			success : function(data) {
				showPoint(map, data);
			},
			error : function(xhr, status, error) {
				$("#div").text("fail");
			}
		});
	});
}

function showPoint(map, data) {
	var features = (new ol.format.GeoJSON({
		featureProjection : 'EPSG:3857'
	})).readFeatures(data);

	var vectorSource = new ol.source.Vector({
		features : features
	});
	var vectorLayer = new ol.layer.Vector(
			{
				source : vectorSource,
				style : new ol.style.Style(
						{
							image : new ol.style.Icon(
									{
										anchor : [ 0.5, 0.5 ],
										anchorXUnits : "fraction",
										anchorYUnits : "fraction",
										src : "https://upload.wikimedia.org/wikipedia/commons/e/ec/RedDot.svg"
									})
						})
			});
	map.addLayer(vectorLayer);
}
