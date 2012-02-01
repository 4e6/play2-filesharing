root = exports ? this

$('#file').change ->
  $('#validateFile').removeClass('ok error')
  validateFile()

$('#url').blur -> validateUrlWithAjax(this)

$('#url').keyup -> validateUrlWithAjax(this)

validateUrlWithAjax = (u) ->
  unless isSaneUrl()
    clearTimeout u.timer if u.timer?
    return u.lastValue = u.value

  if u.value isnt u.lastValue
    clearTimeout u.timer if u.timer?
    $('#validateUrl').removeClass("ok error").html '<img src="/assets/images/ajax-loader.gif" /> checking availability...'

    u.timer = setTimeout(
      -> $.ajax
        url: 'checkUrl'
        type: 'post'
        data:
          'url': $.trim u.value
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
    u.lastValue = u.value

setError = (elem) -> (msg) ->
  elem.removeClass('ok').addClass('error').html msg
  false

isSaneUrl = ->
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

isOkUrl = -> $('#validateUrl').hasClass 'ok'

validateFile = ->
  unless $('#file').val()
    setError($('#validateFile')) 'Please choose a file'
  else
    $('#validateFile').addClass('ok').html 'Ok'

validators = [validateFile, isSaneUrl, isOkUrl]

root.validateForm = ->
  validators.map((v) -> v()).every((r) -> r)
