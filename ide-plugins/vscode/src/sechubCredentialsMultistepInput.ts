import { window, Disposable, ExtensionContext, QuickInputButton, QuickInputButtons, QuickInput, Uri } from 'vscode';
import { SECHUB_CREDENTIAL_KEYS } from './utils/sechubConstants';

export async function multiStepInput(context: ExtensionContext) {

    interface State {
        title: string;
        step: number;
        totalSteps: number;
        serverUrl: string;
        username: string;
        apiToken: string;
    }

    async function collectInputs() {
        const state = {} as Partial<State>;
        await inputStep(new MultiStepInput(), state, 'serverUrl', 'Enter SecHub Server URL', 1);
        await inputStep(new MultiStepInput(), state, 'username', 'Enter SecHub Username', 2);
        await inputStep(new MultiStepInput(), state, 'apiToken', 'Enter SecHub API Token', 3);
        return state as State;
    }

    const title = 'Configure SecHub';

    async function inputStep(input: MultiStepInput, state: Partial<State>, key: keyof typeof SECHUB_CREDENTIAL_KEYS, prompt: string, step: number) {
        state[key] = await input.showInputBox({
            title,
            step,
            totalSteps: 3,
            value: state[key] || '',
            prompt,
            validate: validateNotEmpty,
            shouldResume: shouldResume
        });
    }

    function shouldResume() {
        return new Promise<boolean>((_resolve, _reject) => {
            // noop
        });
    }

    async function validateNotEmpty(value: string) {
        await new Promise(resolve => setTimeout(resolve, 1000));
        return value.trim() === '' ? 'This field cannot be empty' : undefined;
    }

    const state = await collectInputs();
    context.globalState.update(SECHUB_CREDENTIAL_KEYS.serverUrl, state.serverUrl);
    await context.secrets.store(SECHUB_CREDENTIAL_KEYS.username, state.username);
    await context.secrets.store(SECHUB_CREDENTIAL_KEYS.apiToken, state.apiToken);

    window.showInformationMessage(`Configured SecHub with Server URL: '${state.serverUrl}', Username: '${state.username}'`);
}

class InputFlowAction {
    static back = new InputFlowAction();
    static cancel = new InputFlowAction();
    static resume = new InputFlowAction();
}

type InputStep = (input: MultiStepInput) => Thenable<InputStep | void>;

interface InputBoxParameters {
    title: string;
    step: number;
    totalSteps: number;
    value: string;
    prompt: string;
    validate: (value: string) => Promise<string | undefined>;
    buttons?: QuickInputButton[];
    ignoreFocusOut?: boolean;
    placeholder?: string;
    shouldResume: () => Thenable<boolean>;
}

class MultiStepInput {

    static async run(start: InputStep) {
        const input = new MultiStepInput();
        return input.stepThrough(start);
    }

    private current?: QuickInput;
    private steps: InputStep[] = [];

    private async stepThrough(start: InputStep) {
        let step: InputStep | void = start;
        while (step) {
            this.steps.push(step);
            if (this.current) {
                this.current.enabled = false;
                this.current.busy = true;
            }
            try {
                step = await step(this);
            } catch (err) {
                if (err === InputFlowAction.back) {
                    this.steps.pop();
                    step = this.steps.pop();
                } else if (err === InputFlowAction.resume) {
                    step = this.steps.pop();
                } else if (err === InputFlowAction.cancel) {
                    step = undefined;
                } else {
                    throw err;
                }
            }
        }
        if (this.current) {
            this.current.dispose();
        }
    }

    async showInputBox<P extends InputBoxParameters>({ title, step, totalSteps, value, prompt, validate, buttons, ignoreFocusOut, placeholder, shouldResume }: P) {
        const disposables: Disposable[] = [];
        try {
            return await new Promise<string | (P extends { buttons: (infer I)[] } ? I : never)>((resolve, reject) => {
                const input = window.createInputBox();
                input.title = title;
                input.step = step;
                input.totalSteps = totalSteps;
                input.value = value || '';
                input.prompt = prompt;
                input.ignoreFocusOut = ignoreFocusOut ?? false;
                input.placeholder = placeholder;
                input.buttons = [
                    ...(this.steps.length > 1 ? [QuickInputButtons.Back] : []),
                    ...(buttons || [])
                ];
                let validating = validate('');
                disposables.push(
                    input.onDidTriggerButton(item => {
                        if (item === QuickInputButtons.Back) {
                            reject(InputFlowAction.back);
                        } else {
                            resolve(item as any);
                        }
                    }),
                    input.onDidAccept(async () => {
                        const value = input.value;
                        input.enabled = false;
                        input.busy = true;
                        if (!(await validate(value))) {
                            resolve(value);
                        }
                        input.enabled = true;
                        input.busy = false;
                    }),
                    input.onDidChangeValue(async text => {
                        const current = validate(text);
                        validating = current;
                        const validationMessage = await current;
                        if (current === validating) {
                            input.validationMessage = validationMessage;
                        }
                    }),
                    input.onDidHide(() => {
                        (async () => {
                            reject(shouldResume && await shouldResume() ? InputFlowAction.resume : InputFlowAction.cancel);
                        })()
                            .catch(reject);
                    })
                );
                if (this.current) {
                    this.current.dispose();
                }
                this.current = input;
                this.current.show();
            });
        } finally {
            disposables.forEach(d => d.dispose());
        }
    }
}