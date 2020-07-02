#!/usr/bin/env python

# Use NOSECHUB and NOSECHUB-END to mark code fragments
def hello():
  # NOSECHUB
  print("Hello World!")
  # NOSECHUB-END


def add(augend: int, addend: int) -> Int:
  #     NOSECHUB
  return augend + addend
#   NOSECHUB-END

hello()

addition = add(5 + 2)

print(addition)
