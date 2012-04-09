root = exports ? this

# Event handlers
$(document).ready -> $('#upload').submit validateForm

$('#file').change -> validateFile()
$('#url').blur -> validateUrlWithAjax(this)
$('#url').keyup -> validateUrlWithAjax(this)
$('#password').focus -> cleanValidation $('span[id^=password]')
$('#password2').focus -> cleanValidation $('#password2-hint')
$('#question').focus -> cleanValidation $('#question-hint')
$('#answer').focus -> cleanValidation $('#answer-hint')

hintOf = (elem) -> $("##{elem.attr 'id'}-hint")

# Validators
validateFile = ->
  file = $('#file')
  hint = hintOf file
  unless file.val()
    setError hint, 'Please choose a file'
  else
    setSuccess hint

validateUrl = ->
  url = $('#url')
  hint = hintOf url
  unless url.val()
    setError hint, "Please specify a URL"
  else unless hasSuccess url
    url.trigger 'blur'
    false
  else true

validateUrlWithAjax = (u) ->
  hint = hintOf $('#url')
  loader = $('#loader')
  unless u.value
    clearTimeout u.timer if u.timer?
    cleanValidation hint
    loader.hide()
    return u.lastValue = u.value

  if u.value isnt u.lastValue
    clearTimeout u.timer if u.timer?
    cleanValidation hint, "checking availability..."
    loader.show()

    u.timer = setTimeout(
      -> $.ajax
        url: 'checkUrl'
        type: 'post'
        data:
          'url': u.value
        dataType: 'json'
        complete: -> loader.hide()
        success: (j) ->
          if j.available
            setSuccess hint
          else
            setError hint, 'reserved'
        error: (j, msg, error)-> setError hint, msg
      1100
    )
    u.lastValue = u.value

validatePassword = ->
  password = $('#password')
  hint = hintOf password
  password2 = $('#password2')
  hint2 = hintOf password2
  if $('#1').hasClass 'active'
    unless password.val()?.length
      setError hint, 'Enter password'
    else
      setSuccess hint
      unless password2.val()?.length
        setError hint2, 'Retype password'
      else if password.val() isnt password2.val()
        setError hint2, 'Mismatch'
      else
        setSuccess hint2
  else true

validateQuestion = ->
  question = $('#question')
  hint = hintOf question
  if $('#2').hasClass 'active'
    unless question.val()?.length
      setError hint, 'Please specify a question'
    else setSuccess hint
  else true

validateAnswer = ->
  answer = $('#answer')
  hint = hintOf answer
  if $('#2').hasClass 'active'
    unless answer.val()?.length
      setError hint, 'Specify an answer'
    else setSuccess hint
  else true

# Validation
validators = [validateFile, validateUrl, validatePassword, validateQuestion, validateAnswer]

root.validateForm = ->
  validators.map((v) -> v()).every((r) -> r)

# Bootstrap tab switching
$('a[data-toggle="tab"]').on 'show', (e) ->
  passwords = $('input[id^=password]')
  passwordsHint = $('span[id^=password]')
  qa = $("#question,#answer")
  if endsWith(e.relatedTarget.href, '1')
    cleanValidation passwordsHint
    passwords.val ''
    passwords.attr 'disabled', 'disabled'
    qa.removeAttr 'disabled'
  else
    qa.attr 'disabled', 'disabled'
    passwords.removeAttr 'disabled'
