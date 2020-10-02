#!/usr/bin/env python

# SPDX-License-Identifier: MIT

# Use NOSECHUB and NOSECHUB-END to mark code fragments
def hello():
  # NOSECHUB
  print("Hello World!")
  # END-NOSECHUB


def add(augend: int, addend: int) -> Int:
  #     NOSECHUB
  return augend + addend
#   END-NOSECHUB

hello()

addition = add(5 + 2)

print(addition)
