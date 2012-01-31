root = exports ? this

$('#file').change ->
  $('#validateFile').removeClass('ok error')
  isValidFile()

$('#url').keyup ->
  url = @value
  if not isValidUrl()
    clearTimeout @timer if @timer
    return @lastValue = @value

  if @value isnt @lastValue
    clearTimeout @timer if @timer
    $('#validateUrl').removeClass("ok error").html '<img src="/assets/images/ajax-loader.gif" /> checking availability...'

    @timer = setTimeout(
      -> $.ajax
        url: 'checkUrl'
        type: 'post'
        data:
          'url': $.trim url
        dataType: 'json'
        success: (j) ->
          if j.available
            msg = 'available'
            c = 'ok'
          else
            msg = 'reserved'
            c = 'error'
          $('#validateUrl').addClass(c).html msg
      1200
    )
    @lastValue = @value

isValidUrl = ->
  url = $('#url').val()
  handler = setError $('#validateUrl')
  nameCheck = /^[^\s?&]+[^?&]*$/
  wsCheck = /^\S/
  if not url
    handler 'Please specify a URL'
  else if not wsCheck.test url
    handler "URL can't srarts wiht whitespace"
  else if not nameCheck.test url
    handler "URL can't contain '?' and '&' symbols"
  else true

setError = (elem) -> (msg) ->
  elem.removeClass('ok').addClass('error').html msg
  false

isValidFile = ->
  unless $('#file').val()
    setError($('#validateFile')) 'Please choose a file'
  else
    $('#validateFile').addClass('ok').html 'Ok'

/* validUrl will not performen until validFile becomes true */
root.validateForm = ->
  isValidFile() and isValidUrl() and $('#validateUrl').hasClass 'ok'
