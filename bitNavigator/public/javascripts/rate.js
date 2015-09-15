/**
 * Created by ognjen on 14-Sep-15.
 */
$(function(){
    $('.rating-select .btn').on('mouseover', function(){
        $(this).removeClass('btn-default').addClass('btn-warning');
        $(this).prevAll().removeClass('btn-default').addClass('btn-warning');
        $(this).nextAll().removeClass('btn-warning').addClass('btn-default');
    });

    $('.rating-select').on('mouseleave', function(){
        active = $(this).parent().find('.selected');
        if(active.length) {
            active.removeClass('btn-default').addClass('btn-warning');
            active.prevAll().removeClass('btn-default').addClass('btn-warning');
            active.nextAll().removeClass('btn-warning').addClass('btn-default');
        } else {
            $(this).find('.btn').removeClass('btn-warning').addClass('btn-default');
        }
    });

    $('.rating-select .btn').click(function(){
        if($(this).hasClass('selected')) {
            $('.rating-select .selected').removeClass('selected');
            $('#comment-rate').val("0");
        } else {
            $('.rating-select .selected').removeClass('selected');
            $(this).addClass('selected');
            var rate = $(this).data().rate;
            $('#comment-rate').val(rate);
        }
    });
});