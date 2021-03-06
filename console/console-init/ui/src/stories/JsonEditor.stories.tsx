import React from "react";
import { MemoryRouter } from "react-router";
import { JsonEditor } from "components";
import { boolean } from "@storybook/addon-knobs";
import { Page } from "@patternfly/react-core";

export default {
  title: "Json Editor"
};
export const JsonEditorView = () => {
  const jsonData = {
    test: {
      test1: "value1"
    }
  };
  return (
    <MemoryRouter>
      <Page>
        <JsonEditor
          readOnly={boolean("readOnly", true)}
          value={JSON.stringify(jsonData)}
          width="auto"
        />
      </Page>
    </MemoryRouter>
  );
};
