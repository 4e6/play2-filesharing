root = exports ? this

root.endsWith = (str, suffix) ->
  str.indexOf(suffix, str.length - suffix.length) isnt -1

root.setError = (elem, msg) ->
  elem.parent().removeClass('success').addClass('error')
  elem.html msg
  false

root.hasSuccess = (elem) ->
  elem.parent().hasClass('success')

root.setSuccess = (elem) ->
  elem.parent().removeClass('error').addClass('success')
  elem.html '✓'

root.cleanValidation = (elem, msg = '') ->
  elem.parent().removeClass('success error')
  elem.html msg
