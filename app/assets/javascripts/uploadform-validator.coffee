$(document).ready ->
  $('#url').keyup ->
    urlTooltip = $('#validateUrl')
    url = @value
    nameCheck = /^[^\s?&]+[^?&]*$/
    wsCheck = /^\S/
    if not url
      clearTimeout @timer if @timer
      return urlTooltip.html 'Please specify a URL'
    if not wsCheck.test url
      clearTimeout @timer if @timer
      return urlTooltip.html "URL can't starts with whitespace"
    if not nameCheck.test url
      clearTimeout @timer if @timer
      return urlTooltip.html "URL can't contain '?&' symbols"
    if @value isnt @lastValue
      clearTimeout @timer if @timer
      urlTooltip.html '<img src="/assets/images/ajax-loader.gif" /> checking availability...'

      @timer = setTimeout(
        -> $.ajax
          url: 'checkUrl'
          type: 'post'
          data:
            'url': $.trim url
          dataType: 'json'
          success: (j) -> urlTooltip.html j.msg
        1200
      )

      @lastValue = @value