root = exports ? this

# Event handlers
$(document).ready -> $('#upload').submit validateForm

$('#file').change -> validateFile()

$('#url').blur -> validateUrlWithAjax(this)

$('#url').keyup -> validateUrlWithAjax(this)

# Validators
validateFile = ->
  unless $('#file').val()
    setError($('#validateFile')) 'Please choose a file'
  else
    setSuccess $('#validateFile')

validateUrl = ->
  url = $('#url').val()
  handler = setError $('#validateUrl')
  nameCheck = /^[^\s?&]+[^?&]*$/
  wsCheck = /^\S/
  if not url
    handler "Please specify a URL"
  else if not wsCheck.test url
    handler "URL can't srarts with whitespace"
  else if not nameCheck.test url
    handler "URL can't contain '?' and '&' symbols"
  else true

validateUrlWithAjax = (u) ->
  unless validateUrl()
    clearTimeout u.timer if u.timer?
    $('#loader').hide()
    return u.lastValue = u.value

  if u.value isnt u.lastValue
    clearTimeout u.timer if u.timer?
    cleanValidation $('#validateUrl'), "checking availability..."
    $('#loader').show()

    u.timer = setTimeout(
      -> $.ajax
        url: 'checkUrl'
        type: 'post'
        data:
          'url': $.trim u.value
        dataType: 'json'
        complete: -> $('#loader').hide()
        success: (j) ->
          if j.available
            setSuccess $('#validateUrl')
          else
            setError($('#validateUrl')) 'reserved'
      1200
    )
    u.lastValue = u.value

# redundant?
isOkUrl = -> not $('#validateUrl').parent().hasClass 'error'

validatePassword = ->
  if $('#1').hasClass 'active'
    unless $('#password').val()?.length
      setError($('#validatePassword')) 'Enter password'
    else
      setSuccess $('#validatePassword')
  else true

validateQuestion = ->
  if $('#2').hasClass 'active'
    unless $('#question').val()?.length
      setError($('#validateQuestion')) 'Please specify a question'
    else setSuccess $('#validateQuestion')
  else true

validateAnswer = ->
  if $('#2').hasClass 'active'
    unless $('#answer').val()?.length
      setError($('#validateAnswer')) 'specify an answer'
    else setSuccess $('#validateAnswer')
  else true

# Validation
validators = [validateFile, validateUrl, isOkUrl, validatePassword, validateQuestion, validateAnswer]

root.validateForm = ->
  validators.map((v) -> v()).every((r) -> r)

# Bootstrap tab switching
$('a[data-toggle="tab"]').on 'show', (e) ->
  if endsWith(e.relatedTarget.href, '1')
    cleanValidation $('#validatePassword')
    $('#password').val ''
    $('#choice').val 'question'
  else
    $('#choice').val 'password'