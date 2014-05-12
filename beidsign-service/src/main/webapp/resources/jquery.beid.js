 (function($) {
    $.fn.beidicon = function(options) {
        init(this);
        if(options == 'pluggedin')
            pluggedin(this);
        else if(options == 'unplugged')
            unplugged(this);
        else if(options == 'card-inserted')
            cardinserted(this);
        else if(options == 'card-missing')
            cardmissing(this);
        else if(options == 'blink')
            blink(this);
        return this;
    };
    
    function init(obj) {
        if (!obj.hasClass('beidicon')) {
          obj.html('<div class="layer1"></div>\
                    <div class="layer2"></div>\
                    <div class="layer3"></div>\
                    <div class="layer4"></div>\
                    <div class="layer5"></div>');
          obj.addClass('beidicon');
          console.log()
          var offset = obj.height();
          obj.children('div').each(function (i, el) {
            console.log(i);
            $(el).css('background-position', '0 '+(-offset*i)+'px');
          });
        }
    }
    
    function pluggedin(obj) {
        obj.addClass('pluggedin');
    }
    
    function unplugged(obj) {
        obj.removeClass('pluggedin');
    }
    
    function cardinserted(obj) {
        obj.removeClass('card-missing').addClass('card-inserted');
    }
    
    function cardmissing(obj) {
        obj.removeClass('card-inserted').addClass('card-missing');
    }

    function blink(obj) {
        obj.addClass("activity").delay(200).queue(function(){
          $(this).removeClass("activity").dequeue();
        }).delay(200).queue(function(){
          $(this).addClass("activity").dequeue();
        }).delay(200).queue(function(){
          $(this).removeClass("activity").dequeue();
        });
    }
  })(jQuery);
