$(document).ready(function () {
    $('.panel-heading-footer').click(function () {
        $("i", this).toggleClass("fa fa-arrow-circle-o-up fa fa-arrow-circle-o-down");
    })
});
$('#collapseOne').on('show.bs.collapse', function () {
    $('.panel-heading-footer').animate({
        backgroundColor: "#2b669a"
    }, 500);
});

$('#collapseOne').on('hide.bs.collapse', function () {
    $('.panel-heading-footer').animate({
        backgroundColor: "#2b669a"
    }, 500);
});